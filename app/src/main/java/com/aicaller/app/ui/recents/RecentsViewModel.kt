package com.aicaller.app.ui.recents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.data.repository.SpamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentsViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val spamRepository: SpamRepository
) : ViewModel() {

    val callRecords: StateFlow<List<CallRecordEntity>> = callRepository.observeCallRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val assessedRecordIds = mutableSetOf<Long>()

    init {
        refresh()
        assessUnknownNumbers()
    }

    fun refresh() {
        viewModelScope.launch {
            callRepository.syncSystemCallLog()
        }
    }

    /** Runs a quick on-device spam heuristic for unknown numbers and stores the result. */
    private fun assessUnknownNumbers() {
        viewModelScope.launch {
            callRecords.collectLatest { records ->
                records
                    .filter { it.contactName == null && it.spamScore == null && it.id !in assessedRecordIds }
                    .forEach { record ->
                        assessedRecordIds.add(record.id)
                        val assessment = spamRepository.assessNumber(record.phoneNumber, useAi = false)
                        callRepository.updateSpamScore(record.id, assessment.riskScore)
                    }
            }
        }
    }
}
