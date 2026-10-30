package com.aviva.controlfarmacia.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor() {

    /**
     * Writes a down-scaled, EXIF-rotated JPEG of [source] into [target].
     * @throws IOException if [source] cannot be decoded or [target] cannot be written.
     */
    suspend fun compress(
        source: File,
        target: File,
        maxWidth: Int = MAX_SIDE_PX,
        maxHeight: Int = MAX_SIDE_PX,
        quality: Int = JPEG_QUALITY
    ): Unit = withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Cannot decode image ${source.name}")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxWidth, maxHeight)
        }
        val decoded = BitmapFactory.decodeFile(source.absolutePath, options)
            ?: throw IOException("Cannot decode image ${source.name}")
        val upright = rotateToExifOrientation(decoded, source)
        try {
            FileOutputStream(target).use { out ->
                if (!upright.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    throw IOException("Cannot compress image ${source.name}")
                }
            }
        } finally {
            if (upright !== decoded) decoded.recycle()
            upright.recycle()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateToExifOrientation(bitmap: Bitmap, file: File): Bitmap {
        val orientation = ExifInterface(file.absolutePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        const val MAX_SIDE_PX = 1024
        const val JPEG_QUALITY = 80
    }
}
