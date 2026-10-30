package com.aviva.controlfarmacia.ui.home

import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.testutil.FakeMedicationRepository
import com.aviva.controlfarmacia.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)

    // Newest-first order: 1, 3, 2. Expiry order: 2, 1, 3. Name order: 3, 2, 1.
    private val repository = FakeMedicationRepository(
        listOf(
            Medication(id = 1, name = "Paracetamol", expiry = YearMonth.of(2027, 1), createdAt = 3, quantity = 2),
            Medication(
                id = 2, name = "Ibuprofen", patientName = "Luna", expiry = YearMonth.of(2026, 10), createdAt = 1,
                isInTreatment = true, reminderTimes = listOf(LocalTime.of(8, 0))
            ),
            Medication(id = 3, name = "Amoxicillin", patientName = "Ana", expiry = YearMonth.of(2027, 6), createdAt = 2)
        )
    )

    private fun viewModel() = HomeViewModel(repository, clock)

    private suspend fun HomeViewModel.ids(): List<Long> = uiState.first { !it.isLoading }.medications.map { it.id }

    @Test
    fun `blank query lists everything newest first`() = runTest {
        val state = viewModel().uiState.first { !it.isLoading }

        assertEquals(listOf(1L, 3L, 2L), state.medications.map { it.id })
        assertEquals(YearMonth.of(2026, 10), state.today)
        assertEquals(listOf("Ana", "Luna"), state.patients)
    }

    @Test
    fun `query filters by name or patient and ignores surrounding spaces`() = runTest {
        val viewModel = viewModel()

        viewModel.onSearchQueryChange("  luna ")

        val state = viewModel.uiState.first { !it.isLoading && it.medications.size == 1 }
        assertEquals("Ibuprofen", state.medications.single().name)
    }

    @Test
    fun `filters by treatment, expiry and stock`() = runTest {
        val viewModel = viewModel()

        viewModel.onFilterChange(InventoryFilter.IN_TREATMENT)
        assertEquals(listOf(2L), viewModel.ids())

        viewModel.onFilterChange(InventoryFilter.EXPIRING)
        assertEquals(listOf(2L), viewModel.ids())

        viewModel.onFilterChange(InventoryFilter.LOW_STOCK)
        assertEquals(listOf(1L), viewModel.ids())

        viewModel.onFilterChange(InventoryFilter.ALL)
        assertEquals(listOf(1L, 3L, 2L), viewModel.ids())
    }

    @Test
    fun `patient chip filters and toggles off`() = runTest {
        val viewModel = viewModel()

        viewModel.onPatientToggle("Ana")
        assertEquals(listOf(3L), viewModel.ids())

        viewModel.onPatientToggle("Ana")
        assertEquals(listOf(1L, 3L, 2L), viewModel.ids())
    }

    @Test
    fun `sorts by expiry and by name`() = runTest {
        val viewModel = viewModel()

        viewModel.onSortChange(InventorySort.EXPIRY)
        assertEquals(listOf(2L, 1L, 3L), viewModel.ids())

        viewModel.onSortChange(InventorySort.NAME)
        assertEquals(listOf(3L, 2L, 1L), viewModel.ids())
    }
}
