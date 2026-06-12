package com.aicaller.app.ui.spam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SpamListScreen(viewModel: SpamListViewModel = hiltViewModel()) {
    val spamNumbers by viewModel.spamNumbers.collectAsState()

    if (spamNumbers.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("No blocked numbers", style = MaterialTheme.typography.titleLarge)
            Text(
                "Numbers reported as spam or flagged by AI will appear here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(spamNumbers, key = { it.phoneNumber }) { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.phoneNumber, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${entry.source.name.lowercase().replace('_', ' ')} - risk ${entry.riskScore}% - ${entry.reason}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { viewModel.unblock(entry.phoneNumber) }) {
                    Text("Unblock")
                }
            }
            HorizontalDivider()
        }
    }
}
