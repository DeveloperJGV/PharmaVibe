package com.aviva.controlfarmacia.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.domain.model.ExpiryStatus
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val totalMedications: Int = 0,
    val expiringThisMonth: Int = 0,
    val expired: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: MedicationRepository,
    private val clock: Clock
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeAll()
        .map { medications ->
            val today = YearMonth.now(clock)
            val countByStatus = medications.groupingBy { it.expiryStatus(today) }.eachCount()
            DashboardUiState(
                totalMedications = medications.size,
                expiringThisMonth = countByStatus[ExpiryStatus.EXPIRING_THIS_MONTH] ?: 0,
                expired = countByStatus[ExpiryStatus.EXPIRED] ?: 0,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DashboardUiState()
        )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
