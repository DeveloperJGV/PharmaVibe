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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.ui.dashboard.DashboardScreen
import com.aviva.controlfarmacia.ui.detail.MedicationDetailScreen
import com.aviva.controlfarmacia.ui.home.HomeScreen
import com.aviva.controlfarmacia.ui.registration.RegistrationScreen
import com.aviva.controlfarmacia.ui.theme.ControlFarmaciaTheme

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Home : Screen("home")
    object Registration : Screen("registration")
    object Detail : Screen("detail/{medicationId}") {
        fun createRoute(medicationId: Long) = "detail/$medicationId"
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(navController: NavHostController) {
    NavGraphContent(
        navController = navController,
        dashboardScreen = {
            DashboardScreen(
                onNavigateToList = { navController.navigate(Screen.Home.route) }
            )
        },
        homeScreen = { animatedVisibilityScope ->
            HomeScreen(
                onNavigateToRegistration = { navController.navigate(Screen.Registration.route) },
                onNavigateToDetail = { id: Long -> navController.navigate(Screen.Detail.createRoute(id)) },
                animatedVisibilityScope = animatedVisibilityScope
            )
        },
        registrationScreen = {
            RegistrationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        },
        detailScreen = { medicationId, animatedVisibilityScope ->
            MedicationDetailScreen(
                medicationId = medicationId,
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
    detailScreen: @Composable SharedTransitionScope.(Long, AnimatedVisibilityScope) -> Unit
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                dashboardScreen()
            }
            composable(Screen.Home.route) {
                homeScreen(this@composable)
            }
            composable(Screen.Registration.route) {
                registrationScreen()
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: return@composable
                detailScreen(medicationId, this@composable)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun NavGraphPreview() {
    ControlFarmaciaTheme {
        val navController = rememberNavController()
        NavGraphContent(
            navController = navController,
            dashboardScreen = { Text(stringResource(R.string.preview_dashboard_screen)) },
            homeScreen = { Text(stringResource(R.string.preview_home_screen)) },
            registrationScreen = { Text(stringResource(R.string.preview_registration_screen)) },
            detailScreen = { id, _ -> Text(stringResource(R.string.preview_detail_screen_format, id)) }
        )
    }
}
