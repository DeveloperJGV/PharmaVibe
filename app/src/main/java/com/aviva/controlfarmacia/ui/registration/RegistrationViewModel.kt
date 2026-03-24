package com.aviva.controlfarmacia.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import com.aviva.controlfarmacia.data.repository.MedicationRepository
import com.aviva.controlfarmacia.util.LocalMediaManager
import com.aviva.controlfarmacia.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class RegistrationUiState(
    val name: String = "",
    val dosageForm: String = "Tablet",
    val barcode: String = "",
    val expiryMonth: Int = 1,
    val expiryYear: Int = 2024,
    val tempPhotoFile: File? = null,
    val capturedPhotoPath: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val mediaManager: LocalMediaManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDosageFormChange(dosageForm: String) {
        _uiState.update { it.copy(dosageForm = dosageForm) }
    }

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.barcode != barcode) {
            _uiState.update { it.copy(barcode = barcode) }
        }
    }

    fun onExpiryMonthChange(month: Int) {
        _uiState.update { it.copy(expiryMonth = month) }
    }

    fun onExpiryYearChange(year: Int) {
        _uiState.update { it.copy(expiryYear = year) }
    }

    fun onPhotoCaptured(file: File) {
        _uiState.update { it.copy(tempPhotoFile = file, capturedPhotoPath = file.absolutePath) }
    }

    fun saveMedication() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = UiText.StringResource(R.string.error_name_required)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val finalPhotoPath = state.tempPhotoFile?.let { 
                    mediaManager.saveCapturedImage(it)
                }

                val medication = MedicationEntity(
                    name = state.name,
                    dosageForm = state.dosageForm,
                    photoPath = finalPhotoPath,
                    barcode = state.barcode.ifBlank { null },
                    expiryMonth = state.expiryMonth,
                    expiryYear = state.expiryYear
                )
                
                repository.insertMedication(medication)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = e.message?.let { msg -> UiText.DynamicString(msg) } 
                            ?: UiText.StringResource(R.string.error_failed_to_save)
                    ) 
                }
            }
        }
    }
    
    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
