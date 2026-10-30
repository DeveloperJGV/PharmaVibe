package com.aviva.controlfarmacia.domain.media

import java.io.File

/** Owns the photo files of the medications. */
interface MedicationPhotoStore {
    /** A fresh temporary file the camera can write a capture into. */
    fun createCaptureFile(): File

    /**
     * Compresses [capture] into permanent storage and deletes the temporary file.
     * @return the absolute path of the stored photo.
     * @throws java.io.IOException if the photo could not be stored.
     */
    suspend fun persist(capture: File): String

    fun delete(path: String)
}
