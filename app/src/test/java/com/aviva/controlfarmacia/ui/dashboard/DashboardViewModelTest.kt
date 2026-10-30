package com.aviva.controlfarmacia.ui.dashboard

import com.aviva.controlfarmacia.domain.model.Intake
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.testutil.FakeIntakeRepository
import com.aviva.controlfarmacia.testutil.FakeMedicationRepository
import com.aviva.controlfarmacia.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `counts total, expiring, expired, low stock and today's doses`() = runTest {
        val medications = FakeMedicationRepository(
            listOf(
                Medication(id = 1, name = "Old", expiry = YearMonth.of(2026, 1)),
                Medication(id = 2, name = "Older", expiry = YearMonth.of(2025, 12), quantity = 1),
                Medication(
                    id = 3, name = "This month", expiry = YearMonth.of(2026, 10),
                    isInTreatment = true, reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
                ),
                Medication(
                    id = 4, name = "Fine", expiry = YearMonth.of(2027, 6),
                    isInTreatment = true, reminderTimes = listOf(LocalTime.of(9, 0))
                ),
                Medication(id = 5, name = "Paused", expiry = YearMonth.of(2027, 6), reminderTimes = listOf(LocalTime.of(9, 0)))
            )
        )
        val intakes = FakeIntakeRepository(
            initial = listOf(
                Intake(1, 3, LocalDateTime.of(2026, 10, 30, 8, 5)),
                Intake(2, 4, LocalDateTime.of(2026, 10, 30, 9, 1)),
                Intake(3, 3, LocalDateTime.of(2026, 10, 29, 20, 0))
            )
        )

        val state = DashboardViewModel(medications, intakes, clock).uiState.first { !it.isLoading }

        assertEquals(
            DashboardUiState(
                totalMedications = 5,
                expiringThisMonth = 1,
                expired = 2,
                lowStock = 1,
                intakesTakenToday = 2,
                intakesExpectedToday = 3,
                isLoading = false
            ),
            state
        )
    }

    @Test
    fun `empty inventory yields zeros`() = runTest {
        val state = DashboardViewModel(FakeMedicationRepository(), FakeIntakeRepository(), clock)
            .uiState.first { !it.isLoading }

        assertEquals(DashboardUiState(isLoading = false), state)
    }
}
