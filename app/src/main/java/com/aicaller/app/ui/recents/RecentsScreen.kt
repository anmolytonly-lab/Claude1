package com.aicaller.app.ui.recents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicaller.app.data.local.entities.CallDirection
import com.aicaller.app.data.local.entities.CallRecordEntity
import java.text.DateFormat
import java.util.Date

@Composable
fun RecentsScreen(
    onOpenCallDetail: (Long) -> Unit,
    viewModel: RecentsViewModel = hiltViewModel()
) {
    val records by viewModel.callRecords.collectAsState()

    if (records.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No recent calls yet", style = MaterialTheme.typography.titleLarge)
            Text(
                "Calls you make and receive will show up here with AI summaries.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(records, key = { it.id }) { record ->
            CallRecordRow(record = record, onClick = { onOpenCallDetail(record.id) })
        }
    }
}

@Composable
private fun CallRecordRow(record: CallRecordEntity, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectionIcon(record.direction)

            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text = record.contactName ?: record.phoneNumber,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = record.summary ?: record.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(record.startedAt)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RiskBadge(spamScore = record.spamScore)
            }
        }
    }
}

@Composable
private fun RiskBadge(spamScore: Int?) {
    if (spamScore == null) return
    val (label, color) = when {
        spamScore >= 70 -> "High risk $spamScore%" to MaterialTheme.colorScheme.error
        spamScore >= 35 -> "Suspicious $spamScore%" to com.aicaller.app.ui.theme.GeminiAmber
        else -> return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = color
    )
}

@Composable
private fun DirectionIcon(direction: CallDirection) {
    val (icon, color) = when (direction) {
        CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to MaterialTheme.colorScheme.secondary
        CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to MaterialTheme.colorScheme.primary
        CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed to MaterialTheme.colorScheme.error
        CallDirection.BLOCKED -> Icons.Filled.Block to Color.Gray
    }
    Icon(imageVector = icon, contentDescription = direction.name, tint = color)
}
