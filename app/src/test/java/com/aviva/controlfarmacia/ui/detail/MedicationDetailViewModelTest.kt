package com.aviva.controlfarmacia.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.testutil.FakeIntakeRepository
import com.aviva.controlfarmacia.testutil.FakeMedicationRepository
import com.aviva.controlfarmacia.testutil.MainDispatcherRule
import com.aviva.controlfarmacia.testutil.RecordingReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

class MedicationDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)

    private val medication = Medication(id = 5, name = "Paracetamol", expiry = YearMonth.of(2027, 3), quantity = 3)
    private val medications = FakeMedicationRepository(listOf(medication))
    private val intakes = FakeIntakeRepository(medications)
    private val scheduler = RecordingReminderScheduler()

    private fun viewModel() = MedicationDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(MedicationDetailViewModel.MEDICATION_ID_KEY to 5L)),
        repository = medications,
        intakeRepository = intakes,
        reminderScheduler = scheduler,
        clock = clock
    )

    @Test
    fun `exposes the medication and no intakes at first`() = runTest {
        val state = viewModel().uiState.first { !it.isLoading }

        assertEquals(medication, state.medication)
        assertTrue(state.recentIntakes.isEmpty())
        assertEquals(0, state.intakesToday)
    }

    @Test
    fun `recording an intake logs it and consumes one unit`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.recordIntake()

        val state = viewModel.uiState.first { it.recentIntakes.isNotEmpty() }
        assertEquals(1, state.intakesToday)
        assertEquals(2, state.medication?.quantity)
    }

    @Test
    fun `enabling treatment and adding a time re-arms the alarms`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setInTreatment(true)
        viewModel.uiState.first { it.medication?.isInTreatment == true }
        viewModel.addReminderTime(LocalTime.of(8, 0))
        viewModel.uiState.first { it.medication?.reminderTimes?.isNotEmpty() == true }

        assertEquals(2, scheduler.cancelled.size)
        assertEquals(listOf(LocalTime.of(8, 0)), scheduler.scheduled.last().reminderTimes)
    }

    @Test
    fun `delete cancels alarms, removes the row and flags the state`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.delete()

        val state = viewModel.uiState.first { it.isDeleted }
        assertTrue(medications.medications.isEmpty())
        assertEquals(listOf(medication), scheduler.cancelled)
        assertTrue(state.isDeleted)
    }
}
