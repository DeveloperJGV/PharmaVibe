package com.aviva.controlfarmacia.data.repository

import androidx.room.withTransaction
import com.aviva.controlfarmacia.data.local.AppDatabase
import com.aviva.controlfarmacia.data.mapper.toDomain
import com.aviva.controlfarmacia.data.mapper.toEntity
import com.aviva.controlfarmacia.domain.model.Intake
import com.aviva.controlfarmacia.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntakeRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : IntakeRepository {

    private val intakeDao get() = database.intakeDao()
    private val medicationDao get() = database.medicationDao()

    override suspend fun recordIntake(
        medicationId: Long,
        takenAt: LocalDateTime,
        scheduledTime: LocalTime?
    ): Intake = database.withTransaction {
        val intake = Intake(medicationId = medicationId, takenAt = takenAt, scheduledTime = scheduledTime)
        val id = intakeDao.insert(intake.toEntity())
        medicationDao.getById(medicationId)?.toDomain()?.let { medication ->
            if (medication.quantity != null) medicationDao.update(medication.afterIntake().toEntity())
        }
        intake.copy(id = id)
    }

    override fun observeSince(from: LocalDateTime): Flow<List<Intake>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return intakeDao.observeSince(fromMillis).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeForMedication(medicationId: Long, limit: Int): Flow<List<Intake>> =
        intakeDao.observeForMedication(medicationId, limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun delete(intake: Intake) = intakeDao.delete(intake.toEntity())
}
