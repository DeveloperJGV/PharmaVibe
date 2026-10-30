package com.aviva.controlfarmacia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun observeById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications WHERE isCurrentlyInTreatment = 1")
    suspend fun getAllInTreatment(): List<MedicationEntity>

    /** [pattern] must already be a LIKE pattern with `%` and `_` escaped with a backslash. */
    @Query(
        """
        SELECT * FROM medications
        WHERE name LIKE :pattern ESCAPE '\' OR patientName LIKE :pattern ESCAPE '\'
        ORDER BY createdAt DESC
        """
    )
    fun search(pattern: String): Flow<List<MedicationEntity>>
}
