package com.aviva.controlfarmacia.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMediaManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor
) {
    private val mediaDirectory = File(context.filesDir, "medication_images").apply {
        if (!exists()) mkdirs()
    }

    suspend fun saveCapturedImage(tempFile: File): String? = withContext(Dispatchers.IO) {
        try {
            val compressedFile = imageCompressor.compressImage(tempFile)
            val fileName = "med_${UUID.randomUUID()}.jpg"
            val targetFile = File(mediaDirectory, fileName)
            
            compressedFile.inputStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Cleanup temp and compressed files
            if (tempFile.exists()) tempFile.delete()
            if (compressedFile.exists()) compressedFile.delete()
            
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImage(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
