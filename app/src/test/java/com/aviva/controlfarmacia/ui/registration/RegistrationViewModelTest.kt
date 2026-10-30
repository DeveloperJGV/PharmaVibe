package com.aviva.controlfarmacia.ui.registration

import androidx.lifecycle.SavedStateHandle
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.testutil.FakeMedicationRepository
import com.aviva.controlfarmacia.testutil.FakePhotoStore
import com.aviva.controlfarmacia.testutil.MainDispatcherRule
import com.aviva.controlfarmacia.ui.camera.RecognizedText
import com.aviva.controlfarmacia.ui.common.UiText
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)
    private val today = YearMonth.of(2026, 10)

    private fun viewModel(
        repository: FakeMedicationRepository = FakeMedicationRepository(),
        photoStore: FakePhotoStore = FakePhotoStore(),
        editId: Long? = null
    ) = RegistrationViewModel(
        savedStateHandle = SavedStateHandle(editId?.let { mapOf(RegistrationViewModel.MEDICATION_ID_KEY to it) } ?: emptyMap()),
        repository = repository,
        photoStore = photoStore,
        clock = clock
    )

    @Test
    fun `defaults expiry to the current month`() {
        val state = viewModel().uiState.value
        assertEquals(today, state.expiry)
        assertFalse(state.isEdit)
    }

    @Test
    fun `blank name is rejected without saving`() = runTest {
        val repository = FakeMedicationRepository()
        val viewModel = viewModel(repository)

        viewModel.onNameChange("   ")
        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_name_required, (state.error as UiText.Resource).resId)
        assertFalse(state.saveSuccess)
        assertTrue(repository.medications.isEmpty())
    }

    @Test
    fun `saves trimmed fields and omits dosage and quantity when blank`() = runTest {
        val repository = FakeMedicationRepository()
        val viewModel = viewModel(repository)

        viewModel.onNameChange("  Paracetamol ")
        viewModel.onDosageValueChange("   ")
        viewModel.onPatientCategoryChange(PatientCategory.PET)
        viewModel.onPatientNameChange(" Luna ")
        viewModel.onExpiryYearChange(2027)
        viewModel.onExpiryMonthChange(3)
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        val saved = repository.medications.single()
        assertEquals("Paracetamol", saved.name)
        assertNull(saved.dosage)
        assertNull(saved.quantity)
        assertEquals(PatientCategory.PET, saved.patientCategory)
        assertEquals("Luna", saved.patientName)
        assertEquals(YearMonth.of(2027, 3), saved.expiry)
        assertNull(saved.photoPath)
    }

    @Test
    fun `saves dosage with the selected unit and the quantity`() = runTest {
        val repository = FakeMedicationRepository()
        val viewModel = viewModel(repository)

        viewModel.onNameChange("Ibuprofen")
        viewModel.onDosageValueChange("600")
        viewModel.onDosageUnitChange(DosageUnit.TABLET)
        viewModel.onQuantityChange("2x0")
        viewModel.save()

        val saved = repository.medications.single()
        assertEquals(Dosage("600", DosageUnit.TABLET), saved.dosage)
        assertEquals(20, saved.quantity)
    }

    @Test
    fun `expiry never goes before the current month`() {
        val viewModel = viewModel()

        viewModel.onExpiryYearChange(2027)
        viewModel.onExpiryMonthChange(2)
        viewModel.onExpiryYearChange(2026)

        assertEquals(today, viewModel.uiState.value.expiry)
    }

    @Test
    fun `OCR fills the name and a future expiry printed on the box`() {
        val viewModel = viewModel()

        viewModel.onTextRecognized(RecognizedText("AMOXICILINA", "AMOXICILINA 500 mg\nLOTE 1234\nCAD 06/2028"))

        val state = viewModel.uiState.value
        assertEquals("AMOXICILINA", state.name)
        assertEquals(YearMonth.of(2028, 6), state.expiry)
        assertEquals(YearMonth.of(2028, 6), state.recognizedExpiry)
    }

    @Test
    fun `OCR ignores an expiry already in the past`() {
        val viewModel = viewModel()

        viewModel.onTextRecognized(RecognizedText("OLD", "OLD\nCAD 01/2025"))

        assertEquals(today, viewModel.uiState.value.expiry)
        assertNull(viewModel.uiState.value.recognizedExpiry)
    }

    @Test
    fun `photo failure surfaces an error and saves nothing`() = runTest {
        val repository = FakeMedicationRepository()
        val photoStore = FakePhotoStore(failPersist = true)
        val viewModel = viewModel(repository, photoStore)

        viewModel.onNameChange("Paracetamol")
        viewModel.onPhotoCaptured(photoStore.createCaptureFile())
        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_photo_failed, (state.error as UiText.Resource).resId)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertTrue(repository.medications.isEmpty())
    }

    @Test
    fun `persisted photo path is stored with the medication`() = runTest {
        val repository = FakeMedicationRepository()
        val photoStore = FakePhotoStore()
        val viewModel = viewModel(repository, photoStore)
        val capture = photoStore.createCaptureFile()

        viewModel.onNameChange("Paracetamol")
        viewModel.onPhotoCaptured(capture)
        viewModel.save()

        assertEquals(listOf(capture), photoStore.persisted)
        assertEquals("/photos/${capture.name}", repository.medications.single().photoPath)
    }

    @Test
    fun `edit mode prefills the form and keeps what the form does not show`() = runTest {
        val existing = Medication(
            id = 7,
            name = "Paracetamol",
            dosage = Dosage("500", DosageUnit.MILLIGRAM),
            patientName = "Ana",
            isInTreatment = true,
            reminderTimes = listOf(LocalTime.of(8, 0)),
            photoPath = "/photos/old.jpg",
            expiry = YearMonth.of(2027, 3),
            quantity = 12,
            createdAt = 42
        )
        val repository = FakeMedicationRepository(listOf(existing))
        val viewModel = viewModel(repository, editId = 7)

        val loaded = viewModel.uiState.value
        assertTrue(loaded.isEdit)
        assertFalse(loaded.isLoading)
        assertEquals("Paracetamol", loaded.name)
        assertEquals("500", loaded.dosageValue)
        assertEquals("12", loaded.quantity)
        assertEquals("/photos/old.jpg", loaded.existingPhotoPath)

        viewModel.onNameChange("Paracetamol Forte")
        viewModel.onQuantityChange("")
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        val saved = repository.medications.single()
        assertEquals(7L, saved.id)
        assertEquals("Paracetamol Forte", saved.name)
        assertNull(saved.quantity)
        assertEquals(42L, saved.createdAt)
        assertTrue(saved.isInTreatment)
        assertEquals(listOf(LocalTime.of(8, 0)), saved.reminderTimes)
        assertEquals("/photos/old.jpg", saved.photoPath)
    }

    @Test
    fun `editing with a new photo replaces and deletes the old one`() = runTest {
        val existing = Medication(id = 3, name = "X", photoPath = "/photos/old.jpg", expiry = YearMonth.of(2027, 3))
        val repository = FakeMedicationRepository(listOf(existing))
        val photoStore = FakePhotoStore()
        val viewModel = viewModel(repository, photoStore, editId = 3)
        val capture = photoStore.createCaptureFile()

        viewModel.onPhotoCaptured(capture)
        viewModel.save()

        assertEquals("/photos/${capture.name}", repository.medications.single().photoPath)
        assertEquals(listOf("/photos/old.jpg"), photoStore.deleted)
    }

    @Test
    fun `editing a missing medication shows not found`() = runTest {
        val viewModel = viewModel(FakeMedicationRepository(), editId = 99)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(R.string.medication_not_found, (state.error as UiText.Resource).resId)
    }
}
