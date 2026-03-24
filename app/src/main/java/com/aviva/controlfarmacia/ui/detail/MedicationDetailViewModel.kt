package com.aviva.controlfarmacia.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import com.aviva.controlfarmacia.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val medication: MedicationEntity? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadMedication(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val medication = repository.getMedicationById(id)
            if (medication != null) {
                _uiState.update { it.copy(medication = medication, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Medication not found") }
            }
        }
    }

    fun deleteMedication() {
        val medication = _uiState.value.medication ?: return
        viewModelScope.launch {
            repository.deleteMedication(medication)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
