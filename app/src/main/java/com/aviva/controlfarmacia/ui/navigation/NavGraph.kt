package com.aviva.controlfarmacia.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aviva.controlfarmacia.ui.dashboard.DashboardScreen
import com.aviva.controlfarmacia.ui.detail.MedicationDetailScreen
import com.aviva.controlfarmacia.ui.home.HomeScreen
import com.aviva.controlfarmacia.ui.registration.RegistrationScreen

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
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToList = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToRegistration = { navController.navigate(Screen.Registration.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    animatedVisibilityScope = this@composable
                )
            }
            composable(Screen.Registration.route) {
                RegistrationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: return@composable
                MedicationDetailScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() },
                    animatedVisibilityScope = this@composable
                )
            }
        }
    }
}
