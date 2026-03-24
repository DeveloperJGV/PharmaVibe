package com.aviva.controlfarmacia.data.repository

import com.aviva.controlfarmacia.data.local.dao.MedicationDao
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    override suspend fun getMedicationById(id: Long): MedicationEntity? = medicationDao.getMedicationById(id)

    override fun searchMedications(query: String): Flow<List<MedicationEntity>> = medicationDao.searchMedications(query)

    override suspend fun insertMedication(medication: MedicationEntity) = medicationDao.insertMedication(medication)

    override suspend fun updateMedication(medication: MedicationEntity) = medicationDao.updateMedication(medication)

    override suspend fun deleteMedication(medication: MedicationEntity) = medicationDao.deleteMedication(medication)
}
