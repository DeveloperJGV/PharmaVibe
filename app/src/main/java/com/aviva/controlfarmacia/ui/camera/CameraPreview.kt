package com.aviva.controlfarmacia.ui.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Back-camera preview that captures stills through [imageCapture] and streams OCR results to
 * [onTextRecognized]. The camera is released as soon as this leaves the composition.
 */
@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    onTextRecognized: (RecognizedText) -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        CameraPlaceholder(modifier)
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnTextRecognized by rememberUpdatedState(onTextRecognized)

    val previewView = remember { PreviewView(context) }
    val preview = remember {
        Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val textAnalyzer = remember { TextAnalyzer { currentOnTextRecognized(it) } }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(analysisExecutor, textAnalyzer) }
    }

    DisposableEffect(lifecycleOwner, imageCapture) {
        var cameraProvider: ProcessCameraProvider? = null
        val binding = lifecycleOwner.lifecycleScope.launch {
            cameraProvider = ProcessCameraProvider.awaitInstance(context).also { provider ->
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            }
        }
        onDispose {
            binding.cancel()
            cameraProvider?.unbindAll()
            imageAnalysis.clearAnalyzer()
            textAnalyzer.close()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        ScanningOverlay(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CameraPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.camera_preview_placeholder), color = Color.White)
    }
}

/** Four corner brackets framing the area where the medication name should be. */
@Composable
fun ScanningOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val horizontalPadding = 60.dp.toPx()
        val frameHeight = 120.dp.toPx()

        val left = horizontalPadding
        val right = size.width - horizontalPadding
        val top = (size.height - frameHeight) / 2
        val bottom = top + frameHeight

        // Each corner: (x, y, horizontal direction, vertical direction)
        val corners = listOf(
            Corner(left, top, 1f, 1f),
            Corner(right, top, -1f, 1f),
            Corner(left, bottom, 1f, -1f),
            Corner(right, bottom, -1f, -1f)
        )
        corners.forEach { corner ->
            val path = Path().apply {
                moveTo(corner.x, corner.y + cornerLength * corner.dy)
                lineTo(corner.x, corner.y)
                lineTo(corner.x + cornerLength * corner.dx, corner.y)
            }
            drawPath(path = path, color = Color.White, style = Stroke(width = strokeWidth))
        }
    }
}

private data class Corner(val x: Float, val y: Float, val dx: Float, val dy: Float)

@ComposePreview(showBackground = true)
@Composable
private fun CameraPreviewPreview() {
    ControlFarmaciaTheme {
        CameraPreview(imageCapture = ImageCapture.Builder().build(), onTextRecognized = {})
    }
}
