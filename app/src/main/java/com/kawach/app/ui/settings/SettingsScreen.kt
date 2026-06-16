package com.kawach.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kawach.app.KawachApp

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as KawachApp

    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(app.settingsDataStore)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local state for text fields (avoid updating DataStore on every keystroke)
    var fakeCallerName by remember(uiState.fakeCallerName) { mutableStateOf(uiState.fakeCallerName) }
    var localPolice by remember(uiState.localPoliceNumber) { mutableStateOf(uiState.localPoliceNumber) }
    var localCouncillor by remember(uiState.localCouncillorNumber) { mutableStateOf(uiState.localCouncillorNumber) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // ── SOS & Safety ──────────────────────────────────────────────────
        item {
            SettingsSectionHeader("SOS & Safety")
        }

        item {
            SettingsToggleRow(
                icon = Icons.Filled.Vibration,
                title = "Shake-to-SOS",
                subtitle = "फोन जोर से हिलाने पर SOS शुरू होगा",
                checked = uiState.shakeToSos,
                onCheckedChange = viewModel::setShakeToSos
            )
        }

        // ── Privacy ────────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Privacy")
        }

        item {
            SettingsToggleRow(
                icon = Icons.Filled.VisibilityOff,
                title = "Discreet Mode",
                subtitle = "App का launcher icon और नाम बदलें (restart required)",
                checked = uiState.discreetMode,
                onCheckedChange = viewModel::setDiscreetMode
            )
        }

        // ── Fake Call ─────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Fake Call")
        }

        item {
            SettingsTextFieldRow(
                icon = Icons.Filled.PhoneInTalk,
                title = "Fake Caller का नाम",
                subtitle = "Fake Call में यह नाम दिखेगा",
                value = fakeCallerName,
                onValueChange = { fakeCallerName = it },
                onDone = { viewModel.setFakeCallerName(fakeCallerName) },
                placeholder = "जैसे: Papa, Didi, Rahul"
            )
        }

        // ── Local Contacts ────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Local Emergency Numbers")
        }

        item {
            Text(
                "ये numbers आपके area के हैं — खुद fill करें",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        item {
            SettingsTextFieldRow(
                icon = Icons.Filled.LocalPolice,
                title = "Nearest Police Station",
                subtitle = "आपके इलाके का police station",
                value = localPolice,
                onValueChange = { localPolice = it },
                onDone = { viewModel.setLocalPoliceNumber(localPolice) },
                placeholder = "e.g. Abohar PS: 01634-XXXXXX",
                keyboardType = KeyboardType.Phone
            )
        }

        item {
            SettingsTextFieldRow(
                icon = Icons.Filled.AccountBalance,
                title = "Municipal Councillor",
                subtitle = "आपके ward का councillor",
                value = localCouncillor,
                onValueChange = { localCouncillor = it },
                onDone = { viewModel.setLocalCouncillorNumber(localCouncillor) },
                placeholder = "Councillor का नंबर",
                keyboardType = KeyboardType.Phone
            )
        }

        // ── App Info ───────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("App Info")
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Kawach — कवच", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("Version 1.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "कोई data cloud पर नहीं जाता। कोई login नहीं। " +
                            "सब data आपके phone में secure है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    HorizontalDivider()
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingsTextFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                trailingIcon = {
                    if (value.isNotBlank()) {
                        IconButton(onClick = onDone) {
                            Icon(Icons.Filled.Check, contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    }
}
