package com.aicaller.app.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicaller.app.ui.theme.GeminiBlue
import com.aicaller.app.ui.theme.GeminiGreen
import com.aicaller.app.ui.theme.GeminiPurple
import com.aicaller.app.ui.theme.GeminiRed
import kotlin.math.max

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Call,
                    iconColor = GeminiBlue,
                    label = "Total calls",
                    value = uiState.totalCalls.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.PhoneMissed,
                    iconColor = GeminiRed,
                    label = "Missed calls",
                    value = uiState.missedCalls.toString()
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Shield,
                    iconColor = GeminiPurple,
                    label = "Spam blocked",
                    value = uiState.spamBlocked.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Mood,
                    iconColor = GeminiGreen,
                    label = "Avg. sentiment",
                    value = formatSentiment(uiState.averageSentiment)
                )
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Weekly call volume",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    WeeklyVolumeChart(
                        data = uiState.weeklyVolume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
        item {
            Text(
                "Top contacts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (uiState.topContacts.isEmpty()) {
            item {
                Text(
                    "No call history yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.topContacts) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(contact.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${contact.callCount} call${if (contact.callCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyVolumeChart(data: List<DayCallVolume>, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxCount = max(1, data.maxOf { it.count })
        val barAreaHeight = size.height - 24.dp.toPx()
        val barWidth = size.width / (data.size * 2)

        data.forEachIndexed { index, day ->
            val barHeight = (day.count.toFloat() / maxCount) * barAreaHeight
            val x = (index * 2 + 0.5f) * barWidth
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, barAreaHeight - barHeight),
                size = Size(barWidth, max(barHeight, 2.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            val label = textMeasurer.measure(day.label, style = TextStyle(color = labelColor, fontSize = 10.sp))
            drawText(
                textLayoutResult = label,
                topLeft = Offset(x + (barWidth - label.size.width) / 2, barAreaHeight + 4.dp.toPx())
            )
        }

        drawLine(
            color = labelColor.copy(alpha = 0.3f),
            start = Offset(0f, barAreaHeight),
            end = Offset(size.width, barAreaHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun formatSentiment(sentiment: Float): String {
    val percent = ((sentiment + 1f) / 2f * 100f).toInt().coerceIn(0, 100)
    return "$percent%"
}
