package com.aviva.controlfarmacia.domain.repository

import com.aviva.controlfarmacia.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeAll(): Flow<List<Medication>>
    fun observeById(id: Long): Flow<Medication?>
    fun search(query: String): Flow<List<Medication>>
    suspend fun getById(id: Long): Medication?
    suspend fun getAllInTreatment(): List<Medication>
    suspend fun insert(medication: Medication): Long
    suspend fun update(medication: Medication)

    /** Deletes the medication and any photo it owns. */
    suspend fun delete(medication: Medication)
}
