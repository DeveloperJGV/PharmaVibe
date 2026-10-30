package com.aviva.controlfarmacia.ui.registration

import android.database.SQLException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import com.aviva.controlfarmacia.domain.scan.ExpiryDateParser
import com.aviva.controlfarmacia.ui.camera.RecognizedText
import com.aviva.controlfarmacia.ui.common.UiText
import com.aviva.controlfarmacia.ui.navigation.RegistrationRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

data class RegistrationUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val dosageValue: String = "",
    val dosageUnit: DosageUnit = DosageUnit.MILLIGRAM,
    val patientCategory: PatientCategory = PatientCategory.HUMAN,
    val patientName: String = "",
    val expiry: YearMonth = YearMonth.now(),
    /** Last expiry read from the box by OCR, shown as a hint in the camera view. */
    val recognizedExpiry: YearMonth? = null,
    /** Units left, as typed; blank means "not tracked". */
    val quantity: String = "",
    /** Photo already stored for the medication being edited. */
    val existingPhotoPath: String? = null,
    val capturedPhoto: File? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: UiText? = null
) {
    /** What to show in the photo box: a fresh capture wins over the stored photo. */
    val photoModel: Any? get() = capturedPhoto ?: existingPhotoPath
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
    private val photoStore: MedicationPhotoStore,
    clock: Clock
) : ViewModel() {

    // Type-safe navigation stores route fields in the handle under their property names.
    private val route = RegistrationRoute(
        medicationId = savedStateHandle[MEDICATION_ID_KEY] ?: RegistrationRoute.NO_MEDICATION
    )
    private val today: YearMonth = YearMonth.now(clock)

    /** The stored medication when editing; keeps the fields the form does not show. */
    private var editing: Medication? = null

    private val _uiState = MutableStateFlow(RegistrationUiState(isEdit = route.isEdit, isLoading = route.isEdit, expiry = today))
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    init {
        if (route.isEdit) loadForEdit(route.medicationId)
    }

    private fun loadForEdit(id: Long) {
        viewModelScope.launch {
            val medication = repository.getById(id)
            if (medication == null) {
                _uiState.update { it.copy(isLoading = false, error = UiText.Resource(R.string.medication_not_found)) }
                return@launch
            }
            editing = medication
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = medication.name,
                    dosageValue = medication.dosage?.value.orEmpty(),
                    dosageUnit = medication.dosage?.unit ?: DosageUnit.MILLIGRAM,
                    patientCategory = medication.patientCategory,
                    patientName = medication.patientName.orEmpty(),
                    expiry = medication.expiry,
                    quantity = medication.quantity?.toString().orEmpty(),
                    existingPhotoPath = medication.photoPath
                )
            }
        }
    }

    fun createCaptureFile(): File = photoStore.createCaptureFile()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, error = null) }

    /** Fills the name from the box and, when a date is printed on it, the expiry too. */
    fun onTextRecognized(text: RecognizedText) {
        val expiry = ExpiryDateParser.parse(text.fullText, today)?.takeIf { !it.isBefore(today) }
        _uiState.update { state ->
            state.copy(
                name = text.prominent,
                expiry = expiry ?: state.expiry,
                recognizedExpiry = expiry ?: state.recognizedExpiry,
                error = null
            )
        }
    }

    fun onDosageValueChange(value: String) = _uiState.update { it.copy(dosageValue = value) }

    fun onDosageUnitChange(unit: DosageUnit) = _uiState.update { it.copy(dosageUnit = unit) }

    fun onPatientCategoryChange(category: PatientCategory) =
        _uiState.update { it.copy(patientCategory = category) }

    fun onPatientNameChange(name: String) = _uiState.update { it.copy(patientName = name) }

    fun onQuantityChange(value: String) =
        _uiState.update { it.copy(quantity = value.filter(Char::isDigit).take(MAX_QUANTITY_DIGITS)) }

    fun onExpiryMonthChange(month: Int) = _uiState.update { state ->
        state.copy(expiry = YearMonth.of(state.expiry.year, month).coerceAtLeast(today))
    }

    fun onExpiryYearChange(year: Int) = _uiState.update { state ->
        state.copy(expiry = YearMonth.of(year, state.expiry.monthValue).coerceAtLeast(today))
    }

    fun onPhotoCaptured(file: File) = _uiState.update { state ->
        state.capturedPhoto?.takeIf { it != file }?.delete()
        state.copy(capturedPhoto = file, error = null)
    }

    fun onPhotoCaptureFailed() =
        _uiState.update { it.copy(error = UiText.Resource(R.string.error_capture_failed)) }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(error = UiText.Resource(R.string.error_name_required)) }
            return
        }
        if (state.isSaving || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val newPhotoPath = state.capturedPhoto?.let { photoStore.persist(it) }
                val base = editing ?: Medication(name = name, expiry = state.expiry)
                val medication = base.copy(
                    name = name,
                    dosage = state.dosageValue.trim()
                        .takeIf { it.isNotEmpty() }
                        ?.let { Dosage(it, state.dosageUnit) },
                    patientCategory = state.patientCategory,
                    patientName = state.patientName.trim().takeIf { it.isNotEmpty() },
                    photoPath = newPhotoPath ?: base.photoPath,
                    expiry = state.expiry,
                    quantity = state.quantity.toIntOrNull()
                )
                if (editing != null) {
                    repository.update(medication)
                    if (newPhotoPath != null) base.photoPath?.let(photoStore::delete)
                } else {
                    repository.insert(medication)
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isSaving = false, error = UiText.Resource(R.string.error_photo_failed))
                }
            } catch (e: SQLException) {
                _uiState.update {
                    it.copy(isSaving = false, error = UiText.Resource(R.string.error_failed_to_save))
                }
            }
        }
    }

    override fun onCleared() {
        // A capture that was never saved is just a cache file.
        _uiState.value.capturedPhoto?.takeIf { !_uiState.value.saveSuccess }?.delete()
    }

    companion object {
        const val MEDICATION_ID_KEY = "medicationId"
        private const val MAX_QUANTITY_DIGITS = 5
    }
}
