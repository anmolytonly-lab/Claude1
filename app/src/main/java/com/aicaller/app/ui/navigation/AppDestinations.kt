package com.aicaller.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Dialer : AppDestination("dialer", "Dialer", Icons.Filled.Dialpad)
    data object Recents : AppDestination("recents", "Recents", Icons.Filled.History)
    data object Contacts : AppDestination("contacts", "Contacts", Icons.Filled.People)
    data object Insights : AppDestination("insights", "Insights", Icons.Filled.Insights)
    data object Assistant : AppDestination("assistant", "Assistant", Icons.Filled.Mic)
    data object Settings : AppDestination("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(Dialer, Recents, Contacts, Insights, Assistant, Settings)
    }
}

object Routes {
    const val CALL_DETAIL = "call_detail/{callId}"
    const val CONTACT_DETAIL = "contact_detail/{phoneNumber}"
    const val SPAM_LIST = "spam_list"

    fun callDetail(callId: Long) = "call_detail/$callId"
    fun contactDetail(phoneNumber: String) = "contact_detail/${java.net.URLEncoder.encode(phoneNumber, "UTF-8")}"
}
