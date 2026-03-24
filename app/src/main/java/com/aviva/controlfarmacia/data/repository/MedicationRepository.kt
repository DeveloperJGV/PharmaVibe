package com.aviva.controlfarmacia.data.repository

import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: Long): MedicationEntity?
    fun searchMedications(query: String): Flow<List<MedicationEntity>>
    suspend fun insertMedication(medication: MedicationEntity)
    suspend fun updateMedication(medication: MedicationEntity)
    suspend fun deleteMedication(medication: MedicationEntity)
}
