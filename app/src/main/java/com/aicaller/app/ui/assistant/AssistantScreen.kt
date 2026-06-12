package com.aicaller.app.ui.assistant

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicaller.app.ui.theme.GeminiGradient

@Composable
fun AssistantScreen(viewModel: AssistantViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        matches?.firstOrNull()?.let { viewModel.processVoiceCommand(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.callRequests.collect { number ->
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gemini Assistant",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Try \"Call mom\", \"Block this number\", \"Read my last call summary\", or \"Search for the dentist\".",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        FilledIconButton(
            onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command")
                }
                speechLauncher.launch(intent)
            },
            modifier = Modifier
                .size(88.dp)
                .background(brush = Brush.linearGradient(GeminiGradient), shape = MaterialTheme.shapes.extraLarge)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Listen", modifier = Modifier.size(36.dp))
        }

        if (uiState.isProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        }

        uiState.lastHeardText?.let {
            ChatBubble(
                icon = Icons.Filled.Person,
                label = "You said",
                message = it,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        uiState.responseText?.let {
            ChatBubble(
                icon = Icons.Filled.AutoAwesome,
                label = "Gemini",
                message = it,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChatBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    message: String,
    containerColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(16.dp))
                Text(
                    " $label",
                    style = MaterialTheme.typography.labelLarge,
                    color = labelColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
