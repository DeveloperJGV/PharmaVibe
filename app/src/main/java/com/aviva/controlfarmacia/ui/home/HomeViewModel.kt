package com.aviva.controlfarmacia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

data class HomeUiState(
    val medications: List<Medication> = emptyList(),
    val today: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: MedicationRepository,
    private val clock: Clock
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<HomeUiState> = _searchQuery
        .map { it.trim() }
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.observeAll() else repository.search(query)
        }
        .map { HomeUiState(medications = it, today = YearMonth.now(clock), isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeUiState(today = YearMonth.now(clock))
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
