package com.aicaller.app.ui.settings

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onOpenSpamList: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Call screening", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "AI Caller must be set as your call screening app for spam detection " +
                            "and silent-screening to work.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = {
                        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                            !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
                        ) {
                            roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                        }
                    }) {
                        Text("Set as call screening app")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI provider", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Add your Google Gemini API key to enable Gemini-powered summaries, spam analysis, " +
                        "auto-replies and voice commands. Without a key, AI Caller still works using " +
                        "on-device heuristics.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = viewModel.apiKey,
                    onValueChange = viewModel::onApiKeyChanged,
                    label = { Text("Gemini API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Features", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                SettingSwitchRow(
                    title = "AI call screening & spam detection",
                    subtitle = "Analyze unknown callers and silence likely spam",
                    checked = viewModel.spamScreeningEnabled,
                    onCheckedChange = viewModel::onSpamScreeningChanged
                )
                SettingSwitchRow(
                    title = "Auto-block high risk calls",
                    subtitle = "Reject calls the AI is highly confident are spam",
                    checked = viewModel.autoBlockHighRiskCalls,
                    onCheckedChange = viewModel::onAutoBlockChanged
                )
                SettingSwitchRow(
                    title = "Live transcription & summaries",
                    subtitle = "Transcribe calls on-device and generate AI summaries",
                    checked = viewModel.liveTranscriptionEnabled,
                    onCheckedChange = viewModel::onLiveTranscriptionChanged
                )
                SettingSwitchRow(
                    title = "Smart auto-reply",
                    subtitle = "Send an AI-drafted text when you miss a call",
                    checked = viewModel.smartAutoReplyEnabled,
                    onCheckedChange = viewModel::onSmartAutoReplyChanged
                )
            }
        }

        OutlinedButton(onClick = onOpenSpamList, modifier = Modifier.fillMaxWidth()) {
            Text("Manage spam & blocked numbers")
        }
    }
}

@Composable
private fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
