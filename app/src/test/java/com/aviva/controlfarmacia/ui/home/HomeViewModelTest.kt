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
import java.time.YearMonth
import java.time.ZoneOffset

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)

    private val repository = FakeMedicationRepository(
        listOf(
            Medication(id = 1, name = "Paracetamol", expiry = YearMonth.of(2027, 1), createdAt = 1),
            Medication(id = 2, name = "Ibuprofen", patientName = "Luna", expiry = YearMonth.of(2027, 1), createdAt = 2)
        )
    )

    @Test
    fun `blank query lists everything newest first`() = runTest {
        val state = HomeViewModel(repository, clock).uiState.first { !it.isLoading }

        assertEquals(listOf(2L, 1L), state.medications.map { it.id })
        assertEquals(YearMonth.of(2026, 10), state.today)
    }

    @Test
    fun `query filters by name or patient and ignores surrounding spaces`() = runTest {
        val viewModel = HomeViewModel(repository, clock)

        viewModel.onSearchQueryChange("  luna ")

        val state = viewModel.uiState.first { !it.isLoading && it.medications.size == 1 }
        assertEquals("Ibuprofen", state.medications.single().name)
    }
}
