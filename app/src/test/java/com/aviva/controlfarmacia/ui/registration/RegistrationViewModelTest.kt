package com.aviva.controlfarmacia.ui.registration

import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.testutil.FakeMedicationRepository
import com.aviva.controlfarmacia.testutil.FakePhotoStore
import com.aviva.controlfarmacia.testutil.MainDispatcherRule
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
import java.time.YearMonth
import java.time.ZoneOffset

class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)
    private val today = YearMonth.of(2026, 10)

    private fun viewModel(
        repository: FakeMedicationRepository = FakeMedicationRepository(),
        photoStore: FakePhotoStore = FakePhotoStore()
    ) = RegistrationViewModel(repository, photoStore, clock)

    @Test
    fun `defaults expiry to the current month`() {
        assertEquals(today, viewModel().uiState.value.expiry)
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
    fun `saves trimmed fields and omits dosage when value is blank`() = runTest {
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
        assertEquals(PatientCategory.PET, saved.patientCategory)
        assertEquals("Luna", saved.patientName)
        assertEquals(YearMonth.of(2027, 3), saved.expiry)
        assertNull(saved.photoPath)
    }

    @Test
    fun `saves dosage with the selected unit`() = runTest {
        val repository = FakeMedicationRepository()
        val viewModel = viewModel(repository)

        viewModel.onNameChange("Ibuprofen")
        viewModel.onDosageValueChange("600")
        viewModel.onDosageUnitChange(DosageUnit.TABLET)
        viewModel.save()

        assertEquals(Dosage("600", DosageUnit.TABLET), repository.medications.single().dosage)
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
}
