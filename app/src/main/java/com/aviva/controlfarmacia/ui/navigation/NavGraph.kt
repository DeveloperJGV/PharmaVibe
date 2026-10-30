package com.aviva.controlfarmacia.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.ui.dashboard.DashboardScreen
import com.aviva.controlfarmacia.ui.detail.MedicationDetailScreen
import com.aviva.controlfarmacia.ui.home.HomeScreen
import com.aviva.controlfarmacia.ui.registration.RegistrationScreen
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object HomeRoute

@Serializable
data object RegistrationRoute

@Serializable
data class DetailRoute(val medicationId: Long)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(navController: NavHostController) {
    NavGraphContent(
        navController = navController,
        dashboardScreen = {
            DashboardScreen(
                onNavigateToList = { navController.navigate(HomeRoute) { launchSingleTop = true } }
            )
        },
        homeScreen = { animatedVisibilityScope ->
            HomeScreen(
                onNavigateToRegistration = {
                    navController.navigate(RegistrationRoute) { launchSingleTop = true }
                },
                onNavigateToDetail = { id ->
                    navController.navigate(DetailRoute(id)) { launchSingleTop = true }
                },
                animatedVisibilityScope = animatedVisibilityScope
            )
        },
        registrationScreen = {
            RegistrationScreen(onNavigateBack = { navController.popBackStack() })
        },
        detailScreen = { animatedVisibilityScope ->
            MedicationDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraphContent(
    navController: NavHostController,
    dashboardScreen: @Composable () -> Unit,
    homeScreen: @Composable SharedTransitionScope.(AnimatedVisibilityScope) -> Unit,
    registrationScreen: @Composable () -> Unit,
    detailScreen: @Composable SharedTransitionScope.(AnimatedVisibilityScope) -> Unit
) {
    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = DashboardRoute) {
            composable<DashboardRoute> { dashboardScreen() }
            composable<HomeRoute> { homeScreen(this@composable) }
            composable<RegistrationRoute> { registrationScreen() }
            composable<DetailRoute> { detailScreen(this@composable) }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun NavGraphPreview() {
    ControlFarmaciaTheme {
        NavGraphContent(
            navController = rememberNavController(),
            dashboardScreen = { Text(stringResource(R.string.preview_dashboard_screen)) },
            homeScreen = { Text(stringResource(R.string.preview_home_screen)) },
            registrationScreen = { Text(stringResource(R.string.preview_registration_screen)) },
            detailScreen = { Text(stringResource(R.string.preview_detail_screen)) }
        )
    }
}
