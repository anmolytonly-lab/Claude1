package com.kawach.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kawach.app.ui.contacts.ContactsScreen
import com.kawach.app.ui.home.HomeScreen
import com.kawach.app.ui.log.IncidentLogScreen
import com.kawach.app.ui.safetyinfo.SafetyInfoScreen
import com.kawach.app.ui.settings.SettingsScreen

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavRoute("home", "Home", Icons.Filled.Home)
    object Contacts : NavRoute("contacts", "Contacts", Icons.Filled.People)
    object Log : NavRoute("log", "Log", Icons.Filled.Assignment)
    object SafetyInfo : NavRoute("safety_info", "Safety Info", Icons.Filled.HealthAndSafety)
    object Settings : NavRoute("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    NavRoute.Home,
    NavRoute.Contacts,
    NavRoute.Log,
    NavRoute.SafetyInfo,
    NavRoute.Settings
)

@Composable
fun KawachNavGraph(navController: NavHostController = rememberNavController()) {
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backstackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route
        ) {
            composable(NavRoute.Home.route) {
                HomeScreen(paddingValues = paddingValues)
            }
            composable(NavRoute.Contacts.route) {
                ContactsScreen(paddingValues = paddingValues)
            }
            composable(NavRoute.Log.route) {
                IncidentLogScreen(paddingValues = paddingValues)
            }
            composable(NavRoute.SafetyInfo.route) {
                SafetyInfoScreen(paddingValues = paddingValues)
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen(paddingValues = paddingValues)
            }
        }
    }
}
