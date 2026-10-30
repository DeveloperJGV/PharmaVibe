package com.aviva.controlfarmacia.ui.registration

import android.Manifest
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.ui.camera.CameraPreview
import com.aviva.controlfarmacia.ui.common.UiText
import com.aviva.controlfarmacia.ui.common.labelRes
import com.aviva.controlfarmacia.ui.common.nameFieldLabelRes
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.time.YearMonth

/** Callbacks of the registration form, grouped so the content composable stays readable. */
data class RegistrationActions(
    val onNavigateBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onTextRecognized: (String) -> Unit = {},
    val onDosageValueChange: (String) -> Unit = {},
    val onDosageUnitChange: (DosageUnit) -> Unit = {},
    val onPatientCategoryChange: (PatientCategory) -> Unit = {},
    val onPatientNameChange: (String) -> Unit = {},
    val onExpiryMonthChange: (Int) -> Unit = {},
    val onExpiryYearChange: (Int) -> Unit = {},
    val onCreateCaptureFile: () -> File = { File.createTempFile("capture", ".jpg") },
    val onPhotoCaptured: (File) -> Unit = {},
    val onPhotoCaptureFailed: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onRequestCameraPermission: () -> Unit = {}
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onNavigateBack()
    }

    RegistrationContent(
        uiState = uiState,
        today = YearMonth.now(),
        isCameraPermissionGranted = cameraPermissionState.status.isGranted,
        actions = RegistrationActions(
            onNavigateBack = onNavigateBack,
            onNameChange = viewModel::onNameChange,
            onTextRecognized = { text ->
                viewModel.onTextRecognized(text)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDosageValueChange = viewModel::onDosageValueChange,
            onDosageUnitChange = viewModel::onDosageUnitChange,
            onPatientCategoryChange = viewModel::onPatientCategoryChange,
            onPatientNameChange = viewModel::onPatientNameChange,
            onExpiryMonthChange = viewModel::onExpiryMonthChange,
            onExpiryYearChange = viewModel::onExpiryYearChange,
            onCreateCaptureFile = viewModel::createCaptureFile,
            onPhotoCaptured = viewModel::onPhotoCaptured,
            onPhotoCaptureFailed = viewModel::onPhotoCaptureFailed,
            onSave = viewModel::save,
            onRequestCameraPermission = cameraPermissionState::launchPermissionRequest
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationContent(
    uiState: RegistrationUiState,
    today: YearMonth,
    isCameraPermissionGranted: Boolean,
    actions: RegistrationActions,
    modifier: Modifier = Modifier
) {
    val imageCapture = remember { ImageCapture.Builder().build() }
    var showCamera by remember { mutableStateOf(false) }
    val openCamera = {
        if (isCameraPermissionGranted) showCamera = true else actions.onRequestCameraPermission()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!showCamera) {
                TopAppBar(
                    title = { Text(stringResource(R.string.registration_title)) },
                    navigationIcon = {
                        IconButton(onClick = actions.onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (showCamera) {
            CameraCapture(
                imageCapture = imageCapture,
                recognizedName = uiState.name,
                onTextRecognized = actions.onTextRecognized,
                onCreateCaptureFile = actions.onCreateCaptureFile,
                onPhotoCaptured = { file ->
                    actions.onPhotoCaptured(file)
                    showCamera = false
                },
                onPhotoCaptureFailed = {
                    actions.onPhotoCaptureFailed()
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        } else {
            RegistrationForm(
                uiState = uiState,
                today = today,
                actions = actions,
                onOpenCamera = openCamera,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun CameraCapture(
    imageCapture: ImageCapture,
    recognizedName: String,
    onTextRecognized: (String) -> Unit,
    onCreateCaptureFile: () -> File,
    onPhotoCaptured: (File) -> Unit,
    onPhotoCaptureFailed: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(imageCapture = imageCapture, onTextRecognized = onTextRecognized)

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = recognizedName.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = recognizedName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    val file = onCreateCaptureFile()
                    imageCapture.takePicture(
                        ImageCapture.OutputFileOptions.Builder(file).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                onPhotoCaptured(file)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                file.delete()
                                onPhotoCaptureFailed()
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.capture))
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.close_camera),
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationForm(
    uiState: RegistrationUiState,
    today: YearMonth,
    actions: RegistrationActions,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PhotoBox(photo = uiState.capturedPhoto, onOpenCamera = onOpenCamera)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = actions.onNameChange,
            label = { Text(stringResource(R.string.medication_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (uiState.name.isNotEmpty()) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.dosageValue,
                onValueChange = actions.onDosageValueChange,
                label = { Text(stringResource(R.string.dosage_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            DropdownField(
                value = stringResource(uiState.dosageUnit.labelRes()),
                label = stringResource(R.string.dosage_unit_label),
                options = DosageUnit.entries,
                optionLabel = { stringResource(it.labelRes()) },
                onSelect = actions.onDosageUnitChange,
                modifier = Modifier.weight(1f)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val categories = PatientCategory.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                categories.forEachIndexed { index, category ->
                    SegmentedButton(
                        selected = uiState.patientCategory == category,
                        onClick = { actions.onPatientCategoryChange(category) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = categories.size)
                    ) {
                        Text(stringResource(category.labelRes()))
                    }
                }
            }
            OutlinedTextField(
                value = uiState.patientName,
                onValueChange = actions.onPatientNameChange,
                label = { Text(stringResource(uiState.patientCategory.nameFieldLabelRes())) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val firstMonth = if (uiState.expiry.year == today.year) today.monthValue else 1
            DropdownField(
                value = uiState.expiry.monthValue.toTwoDigits(),
                label = stringResource(R.string.month_label),
                options = (firstMonth..12).toList(),
                optionLabel = { it.toTwoDigits() },
                onSelect = actions.onExpiryMonthChange,
                modifier = Modifier.weight(1f)
            )
            DropdownField(
                value = uiState.expiry.year.toString(),
                label = stringResource(R.string.year_label),
                options = (today.year..today.year + YEARS_AHEAD).toList(),
                optionLabel = { it.toString() },
                onSelect = actions.onExpiryYearChange,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = actions.onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PhotoBox(photo: File?, onOpenCamera: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = stringResource(R.string.medication_photo_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            FilledTonalButton(
                onClick = onOpenCamera,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(stringResource(R.string.retake))
            }
        } else {
            Button(onClick = onOpenCamera) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.take_photo_and_scan))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    value: String,
    label: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun Int.toTwoDigits(): String = toString().padStart(2, '0')

private const val YEARS_AHEAD = 10

@Preview(showBackground = true)
@Composable
private fun RegistrationScreenPreview() {
    ControlFarmaciaTheme {
        RegistrationContent(
            uiState = RegistrationUiState(name = "Ibuprofen 600mg", expiry = YearMonth.of(2027, 5)),
            today = YearMonth.of(2026, 10),
            isCameraPermissionGranted = true,
            actions = RegistrationActions()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationScreenErrorPreview() {
    ControlFarmaciaTheme {
        RegistrationContent(
            uiState = RegistrationUiState(
                name = "",
                expiry = YearMonth.of(2026, 10),
                error = UiText.Resource(R.string.error_name_required)
            ),
            today = YearMonth.of(2026, 10),
            isCameraPermissionGranted = false,
            actions = RegistrationActions()
        )
    }
}
