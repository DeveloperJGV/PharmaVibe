package com.aviva.controlfarmacia.data.notification

/** Stable notification / PendingIntent request codes. Keep the ranges disjoint. */
object NotificationIds {
    const val EXPIRED = 1
    const val EXPIRING_SOON = 2
    const val LOW_STOCK = 3

    /** Reminder slots per medication; [reminder] codes are unique below this many times. */
    const val MAX_REMINDERS_PER_MEDICATION = 100
    private const val REMINDER_BASE = 1_000

    fun reminder(medicationId: Long, index: Int): Int {
        require(index in 0 until MAX_REMINDERS_PER_MEDICATION) { "Reminder index out of range: $index" }
        return (REMINDER_BASE + medicationId * MAX_REMINDERS_PER_MEDICATION + index).toInt()
    }
}
