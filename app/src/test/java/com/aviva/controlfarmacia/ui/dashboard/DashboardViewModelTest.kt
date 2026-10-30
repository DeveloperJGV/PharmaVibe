package com.aviva.controlfarmacia.ui.dashboard

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

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `counts total, expiring and expired`() = runTest {
        val repository = FakeMedicationRepository(
            listOf(
                Medication(id = 1, name = "Old", expiry = YearMonth.of(2026, 1)),
                Medication(id = 2, name = "Older", expiry = YearMonth.of(2025, 12)),
                Medication(id = 3, name = "This month", expiry = YearMonth.of(2026, 10)),
                Medication(id = 4, name = "Fine", expiry = YearMonth.of(2027, 6))
            )
        )

        val state = DashboardViewModel(repository, clock).uiState.first { !it.isLoading }

        assertEquals(DashboardUiState(totalMedications = 4, expiringThisMonth = 1, expired = 2, isLoading = false), state)
    }

    @Test
    fun `empty inventory yields zeros`() = runTest {
        val state = DashboardViewModel(FakeMedicationRepository(), clock).uiState.first { !it.isLoading }

        assertEquals(DashboardUiState(isLoading = false), state)
    }
}
