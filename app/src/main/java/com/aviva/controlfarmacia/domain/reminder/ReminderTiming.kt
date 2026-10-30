package com.aviva.controlfarmacia.domain.reminder

import java.time.LocalDateTime
import java.time.LocalTime

object ReminderTiming {
    /** Next moment at or after [now] whose wall-clock time is [time]. */
    fun nextTriggerAt(time: LocalTime, now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate().atTime(time)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }
}
