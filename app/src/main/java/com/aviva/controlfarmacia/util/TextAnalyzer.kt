package com.aviva.controlfarmacia.util

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextAnalyzer(
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val prominentText = findProminentText(visionText)
                    if (prominentText != null) {
                        onTextRecognized(prominentText)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TextAnalyzer", "Text recognition failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun findProminentText(visionText: Text): String? {
        if (visionText.textBlocks.isEmpty()) return null

        // Heuristic: Find the text block with the largest bounding box area
        // or the one closest to the center. Here we'll use area as the primary heuristic.
        return visionText.textBlocks.maxByOrNull { block ->
            val rect = block.boundingBox
            if (rect != null) {
                rect.width() * rect.height()
            } else {
                0
            }
        }?.text?.trim()
    }
}
