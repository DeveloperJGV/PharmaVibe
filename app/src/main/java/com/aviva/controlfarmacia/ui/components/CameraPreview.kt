package com.aviva.controlfarmacia.ui.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import com.aviva.controlfarmacia.util.TextAnalyzer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    onTextRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInspectionMode = LocalInspectionMode.current

    if (isInspectionMode) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.camera_preview_placeholder),
                color = Color.White
            )
        }
        return
    }

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val mainExecutor = ContextCompat.getMainExecutor(context)

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(mainExecutor, TextAnalyzer(onTextRecognized))
            }
    }

    LaunchedEffect(cameraSelector) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Scanning Overlay
        ScanningOverlay(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ScanningOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val padding = 60.dp.toPx()
        
        val width = size.width
        val height = size.height
        
        val rectWidth = width - 2 * padding
        val rectHeight = 120.dp.toPx()
        val left = padding
        val top = (height - rectHeight) / 2
        
        // Draw corners
        // Top-left
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top)
                lineTo(left + cornerLength, top)
            },
            color = Color.White,
            style = Stroke(width = strokeWidth)
        )
        
        // Top-right
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left + rectWidth - cornerLength, top)
                lineTo(left + rectWidth, top)
                lineTo(left + rectWidth, top + cornerLength)
            },
            color = Color.White,
            style = Stroke(width = strokeWidth)
        )
        
        // Bottom-left
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top + rectHeight - cornerLength)
                lineTo(left, top + rectHeight)
                lineTo(left + cornerLength, top + rectHeight)
            },
            color = Color.White,
            style = Stroke(width = strokeWidth)
        )
        
        // Bottom-right
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left + rectWidth - cornerLength, top + rectHeight)
                lineTo(left + rectWidth, top + rectHeight)
                lineTo(left + rectWidth, top + rectHeight - cornerLength)
            },
            color = Color.White,
            style = Stroke(width = strokeWidth)
        )
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProviderFuture ->
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

@ComposePreview(showBackground = true)
@Composable
fun CameraPreviewPreview() {
    ControlFarmaciaTheme {
        CameraPreview(
            imageCapture = ImageCapture.Builder().build(),
            onTextRecognized = {}
        )
    }
}
