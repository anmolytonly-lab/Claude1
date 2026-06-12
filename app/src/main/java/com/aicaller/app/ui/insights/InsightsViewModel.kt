package com.aicaller.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.CallDirection
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.data.repository.SpamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DayCallVolume(val label: String, val count: Int)

data class TopContact(val name: String, val phoneNumber: String, val callCount: Int)

data class InsightsUiState(
    val totalCalls: Int = 0,
    val missedCalls: Int = 0,
    val spamBlocked: Int = 0,
    val averageSentiment: Float = 0f,
    val weeklyVolume: List<DayCallVolume> = emptyList(),
    val topContacts: List<TopContact> = emptyList()
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    callRepository: CallRepository,
    spamRepository: SpamRepository
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        callRepository.observeCallRecords(),
        spamRepository.observeSpamList()
    ) { records, spamList ->
        buildUiState(records, spamList.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())

    private fun buildUiState(records: List<CallRecordEntity>, blockedCount: Int): InsightsUiState {
        val totalCalls = records.size
        val missedCalls = records.count { it.direction == CallDirection.MISSED }

        val sentiments = records.mapNotNull { it.sentiment }
        val averageSentiment = if (sentiments.isEmpty()) 0f else sentiments.sum() / sentiments.size

        val weeklyVolume = buildWeeklyVolume(records)
        val topContacts = buildTopContacts(records)

        return InsightsUiState(
            totalCalls = totalCalls,
            missedCalls = missedCalls,
            spamBlocked = blockedCount,
            averageSentiment = averageSentiment,
            weeklyVolume = weeklyVolume,
            topContacts = topContacts
        )
    }

    private fun buildWeeklyVolume(records: List<CallRecordEntity>): List<DayCallVolume> {
        val calendar = Calendar.getInstance()
        val dayLabels = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        val startOfToday = calendar.clone() as Calendar
        startOfToday.set(Calendar.HOUR_OF_DAY, 0)
        startOfToday.set(Calendar.MINUTE, 0)
        startOfToday.set(Calendar.SECOND, 0)
        startOfToday.set(Calendar.MILLISECOND, 0)

        val dayBuckets = (6 downTo 0).map { offset ->
            val dayStart = startOfToday.clone() as Calendar
            dayStart.add(Calendar.DAY_OF_YEAR, -offset)
            val dayEnd = dayStart.clone() as Calendar
            dayEnd.add(Calendar.DAY_OF_YEAR, 1)
            Triple(dayStart.timeInMillis, dayEnd.timeInMillis, dayLabels[dayStart.get(Calendar.DAY_OF_WEEK) - 1])
        }

        return dayBuckets.map { (start, end, label) ->
            val count = records.count { it.startedAt >= start && it.startedAt < end }
            DayCallVolume(label = label, count = count)
        }
    }

    private fun buildTopContacts(records: List<CallRecordEntity>): List<TopContact> {
        return records
            .groupBy { it.phoneNumber }
            .map { (number, calls) ->
                val name = calls.firstOrNull { it.contactName != null }?.contactName ?: number
                TopContact(name = name, phoneNumber = number, callCount = calls.size)
            }
            .sortedByDescending { it.callCount }
            .take(5)
    }
}
