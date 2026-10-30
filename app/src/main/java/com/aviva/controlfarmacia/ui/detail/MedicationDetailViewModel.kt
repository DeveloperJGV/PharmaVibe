package com.aviva.controlfarmacia.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.domain.model.Intake
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.reminder.ReminderScheduler
import com.aviva.controlfarmacia.domain.repository.IntakeRepository
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class DetailUiState(
    val medication: Medication? = null,
    /** Newest first. */
    val recentIntakes: List<Intake> = emptyList(),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
) {
    val notFound: Boolean get() = !isLoading && medication == null && !isDeleted
    val intakesToday: Int get() = recentIntakes.count { it.takenAt.toLocalDate() == today }
}

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) : ViewModel() {

    // Type-safe navigation stores route fields in the handle under their property names.
    val medicationId: Long = checkNotNull(savedStateHandle[MEDICATION_ID_KEY]) { "DetailRoute needs a medicationId" }
    private val isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<DetailUiState> = combine(
        repository.observeById(medicationId),
        intakeRepository.observeForMedication(medicationId, RECENT_INTAKES),
        isDeleted
    ) { medication, intakes, deleted ->
        DetailUiState(
            medication = medication,
            recentIntakes = intakes,
            today = LocalDate.now(clock),
            isLoading = false,
            isDeleted = deleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DetailUiState(today = LocalDate.now(clock))
    )

    fun setInTreatment(active: Boolean) = updateMedication { it.copy(isInTreatment = active) }

    fun addReminderTime(time: LocalTime) = updateMedication {
        it.copy(reminderTimes = (it.reminderTimes + time).distinct().sorted())
    }

    fun removeReminderTime(time: LocalTime) = updateMedication {
        it.copy(reminderTimes = it.reminderTimes - time)
    }

    fun clearReminderTimes() = updateMedication { it.copy(reminderTimes = emptyList()) }

    /** Logs a dose taken right now (also consumes one unit when quantity is tracked). */
    fun recordIntake() {
        if (uiState.value.medication == null) return
        viewModelScope.launch { intakeRepository.recordIntake(medicationId, LocalDateTime.now(clock)) }
    }

    fun deleteIntake(intake: Intake) {
        viewModelScope.launch { intakeRepository.delete(intake) }
    }

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

    companion object {
        const val MEDICATION_ID_KEY = "medicationId"
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val RECENT_INTAKES = 10
    }
}
