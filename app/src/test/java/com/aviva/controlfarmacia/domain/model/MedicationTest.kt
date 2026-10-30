package com.aviva.controlfarmacia.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.YearMonth

class MedicationTest {

    private val today = YearMonth.of(2026, 10)

    private fun medication(expiry: YearMonth = today, quantity: Int? = null) =
        Medication(name = "Test", expiry = expiry, quantity = quantity)

    @Test
    fun `expiry before current month is expired`() {
        assertEquals(ExpiryStatus.EXPIRED, medication(YearMonth.of(2026, 9)).expiryStatus(today))
        assertEquals(ExpiryStatus.EXPIRED, medication(YearMonth.of(2025, 12)).expiryStatus(today))
    }

    @Test
    fun `expiry in current month is expiring`() {
        assertEquals(ExpiryStatus.EXPIRING_THIS_MONTH, medication(today).expiryStatus(today))
    }

    @Test
    fun `expiry after current month is valid`() {
        assertEquals(ExpiryStatus.VALID, medication(YearMonth.of(2026, 11)).expiryStatus(today))
        assertEquals(ExpiryStatus.VALID, medication(YearMonth.of(2027, 1)).expiryStatus(today))
    }

    @Test
    fun `reminder times are only active while in treatment`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
        val inactive = Medication(name = "Test", expiry = today, reminderTimes = times, isInTreatment = false)
        val active = inactive.copy(isInTreatment = true)

        assertEquals(emptyList<LocalTime>(), inactive.activeReminderTimes)
        assertEquals(times, active.activeReminderTimes)
    }

    @Test
    fun `low stock only applies when quantity is tracked and at or below the threshold`() {
        assertFalse(medication(quantity = null).isLowStock)
        assertFalse(medication(quantity = 6).isLowStock)
        assertTrue(medication(quantity = 5).isLowStock)
        assertTrue(medication(quantity = 0).isLowStock)
        assertFalse(medication(quantity = 5).copy(lowStockThreshold = 2).isLowStock)
    }

    @Test
    fun `an intake consumes one unit but never goes negative`() {
        assertEquals(9, medication(quantity = 10).afterIntake().quantity)
        assertEquals(0, medication(quantity = 0).afterIntake().quantity)
        assertNull(medication(quantity = null).afterIntake().quantity)
    }
}
