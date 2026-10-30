package com.aviva.controlfarmacia.ui.registration

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import com.aviva.controlfarmacia.ui.common.UiText
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
    val name: String = "",
    val dosageValue: String = "",
    val dosageUnit: DosageUnit = DosageUnit.MILLIGRAM,
    val patientCategory: PatientCategory = PatientCategory.HUMAN,
    val patientName: String = "",
    val expiry: YearMonth = YearMonth.now(),
    val capturedPhoto: File? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val photoStore: MedicationPhotoStore,
    clock: Clock
) : ViewModel() {

    private val today: YearMonth = YearMonth.now(clock)

    private val _uiState = MutableStateFlow(RegistrationUiState(expiry = today))
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun createCaptureFile(): File = photoStore.createCaptureFile()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, error = null) }

    fun onTextRecognized(text: String) = _uiState.update { it.copy(name = text, error = null) }

    fun onDosageValueChange(value: String) = _uiState.update { it.copy(dosageValue = value) }

    fun onDosageUnitChange(unit: DosageUnit) = _uiState.update { it.copy(dosageUnit = unit) }

    fun onPatientCategoryChange(category: PatientCategory) =
        _uiState.update { it.copy(patientCategory = category) }

    fun onPatientNameChange(name: String) = _uiState.update { it.copy(patientName = name) }

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
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val photoPath = state.capturedPhoto?.let { photoStore.persist(it) }
                repository.insert(
                    Medication(
                        name = name,
                        dosage = state.dosageValue.trim()
                            .takeIf { it.isNotEmpty() }
                            ?.let { Dosage(it, state.dosageUnit) },
                        patientCategory = state.patientCategory,
                        patientName = state.patientName.trim().takeIf { it.isNotEmpty() },
                        photoPath = photoPath,
                        expiry = state.expiry
                    )
                )
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
}
