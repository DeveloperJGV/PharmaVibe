package com.aviva.controlfarmacia.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdsTest {

    @Test
    fun `reminder ids never collide with expiration ids or each other`() {
        val ids = (1L..50L).flatMap { medicationId ->
            (0 until NotificationIds.MAX_REMINDERS_PER_MEDICATION).map { NotificationIds.reminder(medicationId, it) }
        }

        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.none { it == NotificationIds.EXPIRED || it == NotificationIds.EXPIRING_SOON })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `index outside the slot range is rejected`() {
        NotificationIds.reminder(1, NotificationIds.MAX_REMINDERS_PER_MEDICATION)
    }
}
