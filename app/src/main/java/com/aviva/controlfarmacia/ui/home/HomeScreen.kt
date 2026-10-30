package com.aviva.controlfarmacia.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.ui.common.colors
import com.aviva.controlfarmacia.ui.common.label
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import java.time.YearMonth

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onNavigateToRegistration = onNavigateToRegistration,
        onNavigateToDetail = onNavigateToDetail,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeContent(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.my_pharmacy), fontWeight = FontWeight.Bold) }
                )
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear_search)
                                        )
                                    }
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {}
            }
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToRegistration,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medication))
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> CenteredBox(padding) { CircularProgressIndicator() }
            uiState.medications.isEmpty() -> CenteredBox(padding) {
                Text(
                    text = stringResource(
                        if (searchQuery.isBlank()) R.string.no_medications else R.string.no_search_results
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.medications, key = { it.id }) { medication ->
                    MedicationCard(
                        medication = medication,
                        today = uiState.today,
                        onClick = { onNavigateToDetail(medication.id) },
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MedicationCard(
    medication: Medication,
    today: YearMonth,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val badgeColors = medication.expiryStatus(today).colors()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                if (medication.photoPath != null) {
                    AsyncImage(
                        model = medication.photoPath,
                        contentDescription = medication.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedElement(
                                rememberSharedContentState(key = "image-${medication.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = medication.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    color = badgeColors.container,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = medication.expiry.label(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColors.content
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "name-${medication.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                val subtitle = medication.dosage?.label() ?: medication.patientName
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    val today = YearMonth.of(2026, 10)
    ControlFarmaciaTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeContent(
                    uiState = HomeUiState(
                        medications = listOf(
                            Medication(
                                id = 1,
                                name = "Paracetamol",
                                dosage = Dosage("500", DosageUnit.MILLIGRAM),
                                expiry = today.plusMonths(6)
                            ),
                            Medication(id = 2, name = "Ibuprofen", expiry = today),
                            Medication(id = 3, name = "Amoxicillin", patientName = "Luna", expiry = today.minusMonths(1))
                        ),
                        today = today,
                        isLoading = false
                    ),
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onNavigateToRegistration = {},
                    onNavigateToDetail = {},
                    animatedVisibilityScope = this@AnimatedVisibility
                )
            }
        }
    }
}
