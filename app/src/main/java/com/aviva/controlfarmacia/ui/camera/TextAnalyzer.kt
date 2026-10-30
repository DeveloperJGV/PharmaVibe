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

/** What one analysed frame contained. */
data class RecognizedText(
    /** The largest text block, usually the brand name on the box. */
    val prominent: String,
    /** Every line the recognizer found, for date parsing. */
    val fullText: String
)

/**
 * Runs ML Kit text recognition on each frame and reports the result whenever the prominent
 * text changes. Callbacks arrive on the main thread.
 */
class TextAnalyzer(
    private val onTextRecognized: (RecognizedText) -> Unit
) : ImageAnalysis.Analyzer, Closeable {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastProminent: String? = null

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
                val prominent = findProminentText(result) ?: return@addOnSuccessListener
                if (prominent != lastProminent) {
                    lastProminent = prominent
                    onTextRecognized(RecognizedText(prominent, result.text))
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Text recognition failed", e) }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun close() = recognizer.close()

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
