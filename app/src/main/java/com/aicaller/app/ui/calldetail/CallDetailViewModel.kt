package com.aicaller.app.ui.calldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.data.repository.SpamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val callRepository: CallRepository,
    private val spamRepository: SpamRepository
) : ViewModel() {

    private val callId: Long = checkNotNull(savedStateHandle.get<Long>("callId"))

    private val _record = MutableStateFlow<CallRecordEntity?>(null)
    val record: StateFlow<CallRecordEntity?> = _record.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _record.value = callRepository.getRecord(callId)
        }
    }

    fun requestSummary() {
        val current = _record.value ?: return
        if (current.transcript.isNullOrBlank()) return

        viewModelScope.launch {
            _isSummarizing.value = true
            try {
                callRepository.summarizeNextPendingCall()
                _record.value = callRepository.getRecord(callId)
            } finally {
                _isSummarizing.value = false
            }
        }
    }

    fun reportAsSpam() {
        val current = _record.value ?: return
        viewModelScope.launch {
            spamRepository.reportSpam(current.phoneNumber, "Reported by user from call detail")
            _record.value = callRepository.getRecord(callId)
        }
    }
}
