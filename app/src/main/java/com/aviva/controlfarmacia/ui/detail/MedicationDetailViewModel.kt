package com.aviva.controlfarmacia.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.reminder.ReminderScheduler
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import com.aviva.controlfarmacia.ui.navigation.DetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class DetailUiState(
    val medication: Medication? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
) {
    val notFound: Boolean get() = !isLoading && medication == null && !isDeleted
}

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val medicationId: Long = savedStateHandle.toRoute<DetailRoute>().medicationId
    private val isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<DetailUiState> =
        combine(repository.observeById(medicationId), isDeleted) { medication, deleted ->
            DetailUiState(medication = medication, isLoading = false, isDeleted = deleted)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DetailUiState()
        )

    fun setInTreatment(active: Boolean) = updateMedication { it.copy(isInTreatment = active) }

    fun addReminderTime(time: LocalTime) = updateMedication {
        it.copy(reminderTimes = (it.reminderTimes + time).distinct().sorted())
    }

    fun removeReminderTime(time: LocalTime) = updateMedication {
        it.copy(reminderTimes = it.reminderTimes - time)
    }

    fun clearReminderTimes() = updateMedication { it.copy(reminderTimes = emptyList()) }

    fun delete() {
        val current = uiState.value.medication ?: return
        viewModelScope.launch {
            reminderScheduler.cancel(current)
            repository.delete(current)
            isDeleted.value = true
        }
    }

    /** Persists a change and keeps the scheduled alarms in sync with it. */
    private fun updateMedication(transform: (Medication) -> Medication) {
        val current = uiState.value.medication ?: return
        val updated = transform(current)
        if (updated == current) return
        viewModelScope.launch {
            reminderScheduler.cancel(current)
            repository.update(updated)
            reminderScheduler.schedule(updated)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
