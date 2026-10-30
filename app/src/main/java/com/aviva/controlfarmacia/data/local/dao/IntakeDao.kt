package com.aviva.controlfarmacia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aviva.controlfarmacia.data.local.entity.IntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Insert
    suspend fun insert(intake: IntakeEntity): Long

    @Delete
    suspend fun delete(intake: IntakeEntity)

    @Query("SELECT * FROM intakes WHERE takenAt >= :fromMillis ORDER BY takenAt DESC")
    fun observeSince(fromMillis: Long): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE medicationId = :medicationId ORDER BY takenAt DESC LIMIT :limit")
    fun observeForMedication(medicationId: Long, limit: Int): Flow<List<IntakeEntity>>
}
