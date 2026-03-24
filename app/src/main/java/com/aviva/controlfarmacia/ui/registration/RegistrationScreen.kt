package com.aviva.controlfarmacia.ui.registration

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.ui.components.CameraPreview
import com.aviva.controlfarmacia.util.UiText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import androidx.core.content.ContextCompat
import androidx.compose.ui.tooling.preview.Preview
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import java.util.Calendar

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    RegistrationContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onBarcodeDetected = viewModel::onBarcodeDetected,
        onExpiryMonthChange = viewModel::onExpiryMonthChange,
        onExpiryYearChange = viewModel::onExpiryYearChange,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onSaveMedication = viewModel::saveMedication,
        isCameraPermissionGranted = cameraPermissionState.status.isGranted,
        onRequestCameraPermission = { cameraPermissionState.launchPermissionRequest() }
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegistrationContent(
    uiState: RegistrationUiState,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onExpiryMonthChange: (Int) -> Unit,
    onExpiryYearChange: (Int) -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onSaveMedication: () -> Unit,
    isCameraPermissionGranted: Boolean,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var showCamera by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registration_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (showCamera) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    imageCapture = imageCapture,
                    onBarcodeDetected = onBarcodeDetected
                )
                
                // Camera Controls Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (uiState.barcode.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.barcode_format, uiState.barcode),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                                imageCapture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            onPhotoCaptured(file)
                                            showCamera = false
                                        }
                                        override fun onError(exception: ImageCaptureException) {
                                            // Handle error
                                        }
                                    }
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.capture))
                        }
                    }
                }
                
                IconButton(
                    onClick = { showCamera = false },
                    modifier = Modifier.padding(16.dp).statusBarsPadding().align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.close_camera), tint = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Preview or Camera Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.capturedPhotoPath != null) {
                        AsyncImage(
                            model = uiState.capturedPhotoPath,
                            contentDescription = stringResource(R.string.medication_photo_description),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        FilledTonalButton(
                            onClick = {
                                if (isCameraPermissionGranted) {
                                    showCamera = true
                                } else {
                                    onRequestCameraPermission()
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) {
                            Text(stringResource(R.string.retake))
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isCameraPermissionGranted) {
                                    showCamera = true
                                } else {
                                    onRequestCameraPermission()
                                }
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.take_photo_and_scan))
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.medication_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.barcode,
                    onValueChange = { /* Read only */ },
                    label = { Text(stringResource(R.string.barcode_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        if (uiState.barcode.isNotEmpty()) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green)
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentCalendar = remember { Calendar.getInstance() }
                    val currentYear = currentCalendar.get(Calendar.YEAR)
                    val currentMonth = currentCalendar.get(Calendar.MONTH) + 1

                    var expandedMonth by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.expiryMonth.toString().padStart(2, '0'),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.month_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false }
                        ) {
                            val startMonth = if (uiState.expiryYear == currentYear) currentMonth else 1
                            (startMonth..12).forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month.toString().padStart(2, '0')) },
                                    onClick = {
                                        onExpiryMonthChange(month)
                                        expandedMonth = false
                                    }
                                )
                            }
                        }
                    }

                    var expandedYear by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedYear,
                        onExpandedChange = { expandedYear = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.expiryYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.year_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedYear,
                            onDismissRequest = { expandedYear = false }
                        ) {
                            (currentYear..currentYear + 10).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year.toString()) },
                                    onClick = {
                                        onExpiryYearChange(year)
                                        expandedYear = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onSaveMedication,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.save_medication), fontWeight = FontWeight.Bold)
                    }
                }

                uiState.error?.let { error ->
                    Text(
                        text = error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    ControlFarmaciaTheme {
        RegistrationContent(
            uiState = RegistrationUiState(
                name = "Ibuprofen 600mg",
                barcode = "7891234567890",
                expiryMonth = 5,
                expiryYear = 2026
            ),
            onNavigateBack = {},
            onNameChange = {},
            onBarcodeDetected = {},
            onExpiryMonthChange = {},
            onExpiryYearChange = {},
            onPhotoCaptured = {},
            onSaveMedication = {},
            isCameraPermissionGranted = true,
            onRequestCameraPermission = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenErrorPreview() {
    ControlFarmaciaTheme {
        RegistrationContent(
            uiState = RegistrationUiState(
                name = "Paracetamol",
                error = UiText.DynamicString("Invalid barcode scanned")
            ),
            onNavigateBack = {},
            onNameChange = {},
            onBarcodeDetected = {},
            onExpiryMonthChange = {},
            onExpiryYearChange = {},
            onPhotoCaptured = {},
            onSaveMedication = {},
            isCameraPermissionGranted = false,
            onRequestCameraPermission = {}
        )
    }
}
