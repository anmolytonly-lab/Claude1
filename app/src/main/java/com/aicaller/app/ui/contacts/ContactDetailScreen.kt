package com.aicaller.app.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(viewModel: ContactDetailViewModel = hiltViewModel()) {
    val insight by viewModel.insight.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(insight?.displayName ?: viewModel.phoneNumber, style = MaterialTheme.typography.titleLarge)
        Text(viewModel.phoneNumber, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(onClick = {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${viewModel.phoneNumber}")))
        }) {
            Icon(Icons.Filled.Call, contentDescription = null)
            Text(" Call", modifier = Modifier.padding(start = 4.dp))
        }

        insight?.let { i ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("AI Insights", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                    i.relationship?.let { Text("Relationship: $it") }
                    Text("Total calls analyzed: ${i.totalCalls}")
                    Text("Average sentiment: ${"%.2f".format(i.averageSentiment)}")
                    i.suggestedCallWindow?.let { Text("Best time to call: $it") }
                    i.lastSummary?.let {
                        Text("Last call summary:", style = MaterialTheme.typography.labelLarge)
                        Text(it)
                    }

                    val tags = i.tags.split(",").filter { it.isNotBlank() }
                    if (tags.isNotEmpty()) {
                        Row {
                            tags.forEach { tag ->
                                SuggestionChip(onClick = {}, label = { Text(tag) }, modifier = Modifier.padding(end = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        Text("Call history", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        callHistory.forEach { record ->
            Text("${record.direction} - ${record.durationSeconds}s")
        }
    }
}
