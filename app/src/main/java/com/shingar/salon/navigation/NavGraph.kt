package com.shingar.salon.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shingar.salon.presentation.screens.ai.AiAssistantScreen
import com.shingar.salon.presentation.screens.appointments.AddEditAppointmentScreen
import com.shingar.salon.presentation.screens.appointments.AppointmentsScreen
import com.shingar.salon.presentation.screens.customers.CustomerDetailScreen
import com.shingar.salon.presentation.screens.customers.CustomersScreen
import com.shingar.salon.presentation.screens.dashboard.DashboardScreen
import com.shingar.salon.presentation.screens.login.LoginScreen
import com.shingar.salon.presentation.screens.offers.OffersScreen
import com.shingar.salon.presentation.screens.payments.PaymentsScreen
import com.shingar.salon.presentation.screens.reports.ReportsScreen
import com.shingar.salon.presentation.screens.services.ServicesScreen
import com.shingar.salon.presentation.screens.settings.SettingsScreen
import com.shingar.salon.presentation.screens.splash.SplashScreen
import com.shingar.salon.presentation.screens.staff.StaffScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 100 }) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -100 }) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { 100 }) }
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(navController = navController)
        }

        composable(NavRoutes.APPOINTMENTS) {
            AppointmentsScreen(navController = navController)
        }

        composable(NavRoutes.ADD_APPOINTMENT) {
            AddEditAppointmentScreen(navController = navController)
        }

        composable(
            route = NavRoutes.EDIT_APPOINTMENT,
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AddEditAppointmentScreen(navController = navController, appointmentId = appointmentId)
        }

        composable(NavRoutes.CUSTOMERS) {
            CustomersScreen(navController = navController)
        }

        composable(
            route = NavRoutes.CUSTOMER_DETAIL,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            CustomerDetailScreen(navController = navController, customerId = customerId)
        }

        composable(NavRoutes.SERVICES) {
            ServicesScreen(navController = navController)
        }

        composable(NavRoutes.STAFF) {
            StaffScreen(navController = navController)
        }

        composable(NavRoutes.OFFERS) {
            OffersScreen(navController = navController)
        }

        composable(NavRoutes.PAYMENTS) {
            PaymentsScreen(navController = navController)
        }

        composable(NavRoutes.REPORTS) {
            ReportsScreen(navController = navController)
        }

        composable(NavRoutes.AI_ASSISTANT) {
            AiAssistantScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
