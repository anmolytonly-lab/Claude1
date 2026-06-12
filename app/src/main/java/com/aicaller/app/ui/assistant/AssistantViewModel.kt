package com.aicaller.app.ui.assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.ai.AiClient
import com.aicaller.app.ai.ChatMessage
import com.aicaller.app.ai.ChatRole
import com.aicaller.app.ai.VoiceAction
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.data.repository.ContactRepository
import com.aicaller.app.data.repository.SpamRepository
import com.aicaller.app.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val isProcessing: Boolean = false,
    val lastHeardText: String? = null,
    val responseText: String? = null,
    val transcript: List<ChatMessage> = emptyList()
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiClient: AiClient,
    private val contactRepository: ContactRepository,
    private val callRepository: CallRepository,
    private val spamRepository: SpamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val _callRequests = Channel<String>(capacity = Channel.BUFFERED)
    val callRequests = _callRequests.receiveAsFlow()

    private var textToSpeech: TextToSpeech? = TextToSpeech(context, null)

    fun processVoiceCommand(utterance: String) {
        val historyBefore = _uiState.value.transcript
        _uiState.value = _uiState.value.copy(
            isProcessing = true,
            lastHeardText = utterance,
            responseText = null,
            transcript = historyBefore + ChatMessage(ChatRole.USER, utterance)
        )

        viewModelScope.launch {
            val contacts = contactRepository.getDeviceContacts()
            val result = aiClient.parseVoiceCommand(utterance, contacts.map { it.name })

            val response = when (result.action) {
                VoiceAction.CALL_CONTACT -> handleCall(result.target, contacts)
                VoiceAction.BLOCK_NUMBER -> handleBlock(result.target)
                VoiceAction.READ_LAST_SUMMARY -> handleReadLastSummary()
                VoiceAction.SEARCH_CALL_HISTORY -> handleSearch(result.target)
                VoiceAction.UNKNOWN -> handleChat(utterance, historyBefore)
            }

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                responseText = response,
                transcript = _uiState.value.transcript + ChatMessage(ChatRole.ASSISTANT, response)
            )
            speak(response)
        }
    }

    private suspend fun handleChat(utterance: String, history: List<ChatMessage>): String {
        val recentCalls = callRepository.observeCallRecords().first().take(5)
        val context = if (recentCalls.isEmpty()) {
            ""
        } else {
            recentCalls.joinToString("\n") { record ->
                val who = record.contactName ?: record.phoneNumber
                val summary = record.summary?.let { ": $it" } ?: ""
                "- $who (${record.direction})$summary"
            }
        }
        return aiClient.chat(utterance, history, context)
    }

    private suspend fun handleCall(target: String?, contacts: List<com.aicaller.app.data.repository.Contact>): String {
        if (target.isNullOrBlank()) return "I'm not sure who to call."
        val match = contacts.firstOrNull { it.name.equals(target, ignoreCase = true) }
            ?: contacts.firstOrNull { it.name.contains(target, ignoreCase = true) }

        val number = match?.phoneNumber ?: target.takeIf { it.any(Char::isDigit) }
        return if (number != null) {
            _callRequests.send(number)
            "Calling ${match?.name ?: number}"
        } else {
            "I couldn't find a contact named $target"
        }
    }

    private suspend fun handleBlock(target: String?): String {
        if (target.isNullOrBlank()) return "Please specify a number to block."
        spamRepository.reportSpam(target, "Blocked via voice assistant")
        return "Blocked ${PhoneNumberUtils.normalize(target)}"
    }

    private suspend fun handleReadLastSummary(): String {
        val latest = callRepository.observeCallRecords().first().firstOrNull { it.summary != null }
        return latest?.summary ?: "I don't have a summary for your last call yet."
    }

    private suspend fun handleSearch(target: String?): String {
        if (target.isNullOrBlank()) return "What would you like me to search for?"
        val matches = callRepository.observeCallRecords().first().filter { record ->
            record.contactName?.contains(target, ignoreCase = true) == true ||
                record.phoneNumber.contains(target) ||
                record.summary?.contains(target, ignoreCase = true) == true
        }
        return if (matches.isEmpty()) {
            "I couldn't find any calls matching \"$target\"."
        } else {
            "Found ${matches.size} call(s) matching \"$target\"."
        }
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onCleared() {
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onCleared()
    }
}
