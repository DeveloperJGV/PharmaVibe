package com.aviva.controlfarmacia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviva.controlfarmacia.domain.model.ExpiryStatus
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

enum class InventoryFilter { ALL, IN_TREATMENT, EXPIRING, LOW_STOCK }

enum class InventorySort { NEWEST, EXPIRY, NAME }

data class InventoryOptions(
    val filter: InventoryFilter = InventoryFilter.ALL,
    /** Only medications of this patient/pet; `null` means everyone. */
    val patient: String? = null,
    val sort: InventorySort = InventorySort.NEWEST
)

data class HomeUiState(
    val medications: List<Medication> = emptyList(),
    val today: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val options: InventoryOptions = InventoryOptions(),
    /** Every patient name in the inventory, for the patient chips. */
    val patients: List<String> = emptyList()
) {
    val isFiltered: Boolean
        get() = options.filter != InventoryFilter.ALL || options.patient != null
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: MedicationRepository,
    private val clock: Clock
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val options = MutableStateFlow(InventoryOptions())

    private val searched = _searchQuery
        .map { it.trim() }
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.observeAll() else repository.search(query)
        }

    val uiState: StateFlow<HomeUiState> = combine(searched, options) { medications, opts ->
        val today = YearMonth.now(clock)
        HomeUiState(
            medications = medications.applyOptions(opts, today),
            today = today,
            isLoading = false,
            options = opts,
            patients = medications.mapNotNull { it.patientName }.distinct().sorted()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = HomeUiState(today = YearMonth.now(clock))
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: InventoryFilter) = options.update { it.copy(filter = filter) }

    /** Selecting the active patient again clears the patient filter. */
    fun onPatientToggle(patient: String) =
        options.update { it.copy(patient = if (it.patient == patient) null else patient) }

    fun onSortChange(sort: InventorySort) = options.update { it.copy(sort = sort) }

    private fun List<Medication>.applyOptions(opts: InventoryOptions, today: YearMonth): List<Medication> {
        val filtered = filter { medication ->
            val byFilter = when (opts.filter) {
                InventoryFilter.ALL -> true
                InventoryFilter.IN_TREATMENT -> medication.isInTreatment
                InventoryFilter.EXPIRING -> medication.expiryStatus(today) != ExpiryStatus.VALID
                InventoryFilter.LOW_STOCK -> medication.isLowStock
            }
            byFilter && (opts.patient == null || medication.patientName == opts.patient)
        }
        return when (opts.sort) {
            InventorySort.NEWEST -> filtered.sortedByDescending { it.createdAt }
            InventorySort.EXPIRY -> filtered.sortedWith(compareBy({ it.expiry }, { it.name.lowercase() }))
            InventorySort.NAME -> filtered.sortedBy { it.name.lowercase() }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
