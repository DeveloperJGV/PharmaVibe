package com.aviva.controlfarmacia.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.ui.common.label
import com.aviva.controlfarmacia.ui.common.rememberNotificationPermissionRequest
import com.aviva.controlfarmacia.ui.common.toDisplayString
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import java.time.LocalTime
import java.time.YearMonth

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MedicationDetailScreen(
    onNavigateBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: MedicationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateBack()
    }

    MedicationDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDelete = viewModel::delete,
        onSetInTreatment = viewModel::setInTreatment,
        onAddReminderTime = viewModel::addReminderTime,
        onRemoveReminderTime = viewModel::removeReminderTime,
        onClearReminderTimes = viewModel::clearReminderTimes,
        sharedTransitionScope = this,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MedicationDetailContent(
    uiState: DetailUiState,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit,
    onSetInTreatment: (Boolean) -> Unit,
    onAddReminderTime: (LocalTime) -> Unit,
    onRemoveReminderTime: (LocalTime) -> Unit,
    onClearReminderTimes: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState.medication != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        val medication = uiState.medication
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            medication == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.notFound) {
                    Text(stringResource(R.string.medication_not_found), style = MaterialTheme.typography.bodyLarge)
                }
            }

            else -> MedicationDetails(
                medication = medication,
                onSetInTreatment = onSetInTreatment,
                onAddReminderTime = onAddReminderTime,
                onRemoveReminderTime = onRemoveReminderTime,
                onClearReminderTimes = onClearReminderTimes,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_dialog_title),
            text = stringResource(R.string.delete_dialog_text, uiState.medication?.name.orEmpty()),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MedicationDetails(
    medication: Medication,
    onSetInTreatment: (Boolean) -> Unit,
    onAddReminderTime: (LocalTime) -> Unit,
    onRemoveReminderTime: (LocalTime) -> Unit,
    onClearReminderTimes: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (medication.photoPath != null) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = medication.photoPath,
                        contentDescription = medication.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedElement(
                                rememberSharedContentState(key = "image-${medication.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = medication.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                with(sharedTransitionScope) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "name-${medication.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                }
                medication.dosage?.let { dosage ->
                    Text(
                        text = dosage.label(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            HorizontalDivider()

            DetailItem(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.expiration_date_label),
                value = medication.expiry.label()
            )

            medication.patientName?.let { patientName ->
                val isPet = medication.patientCategory == PatientCategory.PET
                DetailItem(
                    icon = if (isPet) Icons.Default.Pets else Icons.Default.Person,
                    label = stringResource(if (isPet) R.string.pet_name_label else R.string.patient_name_detail_label),
                    value = patientName
                )
            }

            HorizontalDivider()

            TreatmentSection(
                medication = medication,
                onSetInTreatment = onSetInTreatment,
                onAddReminderTime = onAddReminderTime,
                onRemoveReminderTime = onRemoveReminderTime,
                onClearReminderTimes = onClearReminderTimes
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreatmentSection(
    medication: Medication,
    onSetInTreatment: (Boolean) -> Unit,
    onAddReminderTime: (LocalTime) -> Unit,
    onRemoveReminderTime: (LocalTime) -> Unit,
    onClearReminderTimes: () -> Unit
) {
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    var showClearDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        val active = medication.isInTreatment
        Surface(
            color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.active_treatment_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.active_treatment_subtitle), style = MaterialTheme.typography.labelMedium)
        }
        Switch(
            checked = active,
            onCheckedChange = { isActive ->
                if (isActive) requestNotificationPermission()
                onSetInTreatment(isActive)
            }
        )
    }

    AnimatedVisibility(visible = medication.isInTreatment) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.schedule_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (medication.reminderTimes.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text(stringResource(R.string.clear_schedule), color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            medication.reminderTimes.forEach { time ->
                ReminderTimeRow(time = time, onRemove = { onRemoveReminderTime(time) })
            }

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_reminder_time))
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = stringResource(R.string.clear_schedule_dialog_title),
            text = stringResource(R.string.clear_schedule_dialog_text),
            confirmLabel = stringResource(R.string.clear),
            onConfirm = {
                showClearDialog = false
                onClearReminderTimes()
            },
            onDismiss = { showClearDialog = false }
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onAddReminderTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
private fun ReminderTimeRow(time: LocalTime, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(
                text = time.toDisplayString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_reminder_time))
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun MedicationDetailPreview() {
    ControlFarmaciaTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                MedicationDetailContent(
                    uiState = DetailUiState(
                        medication = Medication(
                            id = 1,
                            name = "Paracetamol",
                            dosage = Dosage("500", DosageUnit.MILLIGRAM),
                            patientCategory = PatientCategory.PET,
                            patientName = "Luna",
                            isInTreatment = true,
                            reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 30)),
                            expiry = YearMonth.of(2027, 12)
                        ),
                        isLoading = false
                    ),
                    onNavigateBack = {},
                    onDelete = {},
                    onSetInTreatment = {},
                    onAddReminderTime = {},
                    onRemoveReminderTime = {},
                    onClearReminderTimes = {},
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedVisibility
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailItemPreview() {
    ControlFarmaciaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DetailItem(
                icon = Icons.Default.Medication,
                label = stringResource(R.string.expiration_date_label),
                value = "12/2027"
            )
        }
    }
}
