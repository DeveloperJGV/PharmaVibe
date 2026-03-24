package com.aviva.controlfarmacia.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import com.aviva.controlfarmacia.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val totalMedications: Int = 0,
    val expiringThisMonth: Int = 0,
    val expired: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllMedications()
        .map { medications ->
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            val expiringThisMonth = medications.count { 
                it.expiryMonth == currentMonth && it.expiryYear == currentYear 
            }
            
            val expired = medications.count { 
                it.expiryYear < currentYear || (it.expiryYear == currentYear && it.expiryMonth < currentMonth)
            }

            DashboardUiState(
                totalMedications = medications.size,
                expiringThisMonth = expiringThisMonth,
                expired = expired,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
}
