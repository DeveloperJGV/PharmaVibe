package com.aviva.controlfarmacia.data.media

import android.content.Context
import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps medication photos as compressed JPEGs under the app's private files directory. */
@Singleton
class LocalMedicationPhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor
) : MedicationPhotoStore {

    private val photoDirectory: File
        get() = File(context.filesDir, PHOTO_DIRECTORY).apply { mkdirs() }

    override fun createCaptureFile(): File =
        File(context.cacheDir, "capture_${UUID.randomUUID()}.jpg")

    override suspend fun persist(capture: File): String = withContext(Dispatchers.IO) {
        val target = File(photoDirectory, "med_${UUID.randomUUID()}.jpg")
        try {
            imageCompressor.compress(capture, target)
        } catch (e: Exception) {
            target.delete()
            throw e
        } finally {
            capture.delete()
        }
        target.absolutePath
    }

    override fun delete(path: String) {
        File(path).delete()
    }

    private companion object {
        const val PHOTO_DIRECTORY = "medication_images"
    }
}
