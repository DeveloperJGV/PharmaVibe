package com.aviva.controlfarmacia.ui.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable

/**
 * Runs ML Kit text recognition on each frame and reports the most prominent text block
 * whenever it changes. Callbacks arrive on the main thread.
 */
class TextAnalyzer(
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer, Closeable {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastText: String? = null

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = findProminentText(result) ?: return@addOnSuccessListener
                if (text != lastText) {
                    lastText = text
                    onTextRecognized(text)
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Text recognition failed", e) }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun close() = recognizer.close()

    /** The block with the largest bounding box is usually the brand name on a box. */
    private fun findProminentText(visionText: Text): String? =
        visionText.textBlocks
            .maxByOrNull { block -> block.boundingBox?.let { it.width() * it.height() } ?: 0 }
            ?.text
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private companion object {
        const val TAG = "TextAnalyzer"
    }
}
