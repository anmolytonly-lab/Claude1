package com.aicaller.app.ui.calldetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun CallDetailScreen(viewModel: CallDetailViewModel = hiltViewModel()) {
    val record by viewModel.record.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()

    val current = record
    if (current == null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Loading call...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(current.contactName ?: current.phoneNumber, style = MaterialTheme.typography.titleLarge)
        Text(
            DateFormat.getDateTimeInstance().format(Date(current.startedAt)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (current.spamScore != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Spam risk: ${current.spamScore}%", style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        SectionCard(title = "AI Summary") {
            when {
                current.summary != null -> Text(current.summary, style = MaterialTheme.typography.bodyLarge)
                current.transcript == null -> Text(
                    "No transcript was captured for this call.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                isSummarizing -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                else -> Button(onClick = { viewModel.requestSummary() }) {
                    Text("Generate AI summary")
                }
            }
        }

        if (!current.actionItems.isNullOrBlank()) {
            SectionCard(title = "Action items") {
                current.actionItems.split("\n").filter { it.isNotBlank() }.forEach { item ->
                    Text("• $item", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (current.sentiment != null) {
            SectionCard(title = "Sentiment") {
                Text(sentimentLabel(current.sentiment), style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (!current.transcript.isNullOrBlank()) {
            SectionCard(title = "Transcript") {
                Text(current.transcript, style = MaterialTheme.typography.bodyLarge)
            }
        }

        OutlinedButton(onClick = { viewModel.reportAsSpam() }) {
            Text("Report as spam")
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

private fun sentimentLabel(sentiment: Float): String = when {
    sentiment > 0.3f -> "Positive (${"%.1f".format(sentiment)})"
    sentiment < -0.3f -> "Negative (${"%.1f".format(sentiment)})"
    else -> "Neutral (${"%.1f".format(sentiment)})"
}
