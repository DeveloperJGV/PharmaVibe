package com.aviva.controlfarmacia.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTimingTest {

    private val now = LocalDateTime.of(2026, 10, 30, 12, 0)

    @Test
    fun `time later today triggers today`() {
        val trigger = ReminderTiming.nextTriggerAt(LocalTime.of(20, 30), now)
        assertEquals(LocalDateTime.of(2026, 10, 30, 20, 30), trigger)
    }

    @Test
    fun `time already passed today triggers tomorrow`() {
        val trigger = ReminderTiming.nextTriggerAt(LocalTime.of(8, 0), now)
        assertEquals(LocalDateTime.of(2026, 10, 31, 8, 0), trigger)
    }

    @Test
    fun `exact current minute triggers tomorrow, never in the past`() {
        val trigger = ReminderTiming.nextTriggerAt(LocalTime.of(12, 0), now)
        assertEquals(LocalDateTime.of(2026, 10, 31, 12, 0), trigger)
    }

    @Test
    fun `rolls over month boundaries`() {
        val lastDay = LocalDateTime.of(2026, 10, 31, 23, 0)
        val trigger = ReminderTiming.nextTriggerAt(LocalTime.of(7, 0), lastDay)
        assertEquals(LocalDateTime.of(2026, 11, 1, 7, 0), trigger)
    }
}
