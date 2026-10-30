package com.aviva.controlfarmacia.testutil

import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.reminder.ReminderScheduler
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.IOException

/** In-memory repository; search matches name or patient name, case-insensitively. */
class FakeMedicationRepository(initial: List<Medication> = emptyList()) : MedicationRepository {
    private val store = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    val medications: List<Medication> get() = store.value

    override fun observeAll(): Flow<List<Medication>> = store.map { it.sortedByDescending { m -> m.createdAt } }

    override fun observeById(id: Long): Flow<Medication?> = store.map { list -> list.firstOrNull { it.id == id } }

    override fun search(query: String): Flow<List<Medication>> = store.map { list ->
        list.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.patientName?.contains(query, ignoreCase = true) == true
        }
    }

    override suspend fun getById(id: Long): Medication? = store.value.firstOrNull { it.id == id }

    override suspend fun getAllInTreatment(): List<Medication> = store.value.filter { it.isInTreatment }

    override suspend fun insert(medication: Medication): Long {
        val id = if (medication.id == 0L) nextId++ else medication.id
        store.update { it.filterNot { m -> m.id == id } + medication.copy(id = id) }
        return id
    }

    override suspend fun update(medication: Medication) {
        store.update { list -> list.map { if (it.id == medication.id) medication else it } }
    }

    override suspend fun delete(medication: Medication) {
        store.update { list -> list.filterNot { it.id == medication.id } }
    }
}

class FakePhotoStore(private val failPersist: Boolean = false) : MedicationPhotoStore {
    val persisted = mutableListOf<File>()
    val deleted = mutableListOf<String>()

    override fun createCaptureFile(): File = File.createTempFile("capture", ".jpg").apply { deleteOnExit() }

    override suspend fun persist(capture: File): String {
        if (failPersist) throw IOException("boom")
        persisted += capture
        return "/photos/${capture.name}"
    }

    override fun delete(path: String) {
        deleted += path
    }
}

class RecordingReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Medication>()
    val cancelled = mutableListOf<Medication>()

    override fun schedule(medication: Medication) {
        scheduled += medication
    }

    override fun cancel(medication: Medication) {
        cancelled += medication
    }
}
