package com.aicaller.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aicaller.app.ui.assistant.AssistantScreen
import com.aicaller.app.ui.calldetail.CallDetailScreen
import com.aicaller.app.ui.contacts.ContactDetailScreen
import com.aicaller.app.ui.contacts.ContactsScreen
import com.aicaller.app.ui.dialer.DialerScreen
import com.aicaller.app.ui.recents.RecentsScreen
import com.aicaller.app.ui.settings.SettingsScreen
import com.aicaller.app.ui.spam.SpamListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = AppDestination.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle(currentRoute)) },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    AppDestination.bottomNavItems.forEach { destination ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Dialer.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(AppDestination.Dialer.route) { DialerScreen() }
            composable(AppDestination.Recents.route) {
                RecentsScreen(onOpenCallDetail = { callId -> navController.navigate(Routes.callDetail(callId)) })
            }
            composable(AppDestination.Contacts.route) {
                ContactsScreen(onOpenContact = { number -> navController.navigate(Routes.contactDetail(number)) })
            }
            composable(AppDestination.Assistant.route) { AssistantScreen() }
            composable(AppDestination.Settings.route) {
                SettingsScreen(onOpenSpamList = { navController.navigate(Routes.SPAM_LIST) })
            }
            composable(
                route = Routes.CALL_DETAIL,
                arguments = listOf(navArgument("callId") { type = NavType.LongType })
            ) {
                CallDetailScreen()
            }
            composable(
                route = Routes.CONTACT_DETAIL,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) {
                ContactDetailScreen()
            }
            composable(Routes.SPAM_LIST) { SpamListScreen() }
        }
    }
}

private fun topBarTitle(route: String?): String = when (route) {
    AppDestination.Dialer.route -> "AI Caller"
    AppDestination.Recents.route -> "Recents"
    AppDestination.Contacts.route -> "Contacts"
    AppDestination.Assistant.route -> "Assistant"
    AppDestination.Settings.route -> "Settings"
    Routes.SPAM_LIST -> "Spam & blocked numbers"
    else -> "AI Caller"
}
