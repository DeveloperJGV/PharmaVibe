package com.aviva.controlfarmacia.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/** One recorded dose of a medication. */
data class Intake(
    val id: Long = 0,
    val medicationId: Long,
    val takenAt: LocalDateTime,
    /** Reminder slot this intake answered, if it came from a reminder. */
    val scheduledTime: LocalTime? = null
)
