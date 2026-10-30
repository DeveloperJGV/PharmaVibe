package com.aviva.controlfarmacia.domain.repository

import com.aviva.controlfarmacia.domain.model.Intake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.LocalTime

interface IntakeRepository {
    /**
     * Records a dose and, when the medication tracks its quantity, consumes one unit.
     * @return the intake that was stored.
     */
    suspend fun recordIntake(
        medicationId: Long,
        takenAt: LocalDateTime,
        scheduledTime: LocalTime? = null
    ): Intake

    /** Intakes of every medication taken at or after [from], newest first. */
    fun observeSince(from: LocalDateTime): Flow<List<Intake>>

    /** Latest [limit] intakes of one medication, newest first. */
    fun observeForMedication(medicationId: Long, limit: Int): Flow<List<Intake>>

    suspend fun delete(intake: Intake)
}
