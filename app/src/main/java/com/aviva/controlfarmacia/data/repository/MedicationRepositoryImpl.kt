package com.aviva.controlfarmacia.data.repository

import com.aviva.controlfarmacia.data.local.dao.MedicationDao
import com.aviva.controlfarmacia.data.mapper.toDomain
import com.aviva.controlfarmacia.data.mapper.toEntity
import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val dao: MedicationDao,
    private val photoStore: MedicationPhotoStore
) : MedicationRepository {

    override fun observeAll(): Flow<List<Medication>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Medication?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun search(query: String): Flow<List<Medication>> =
        dao.search(query.toLikePattern()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): Medication? = dao.getById(id)?.toDomain()

    override suspend fun getAllInTreatment(): List<Medication> =
        dao.getAllInTreatment().map { it.toDomain() }

    override suspend fun insert(medication: Medication): Long = dao.insert(medication.toEntity())

    override suspend fun update(medication: Medication) = dao.update(medication.toEntity())

    override suspend fun delete(medication: Medication) {
        dao.delete(medication.toEntity())
        medication.photoPath?.let(photoStore::delete)
    }

    private fun String.toLikePattern(): String {
        val escaped = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        return "%$escaped%"
    }
}
