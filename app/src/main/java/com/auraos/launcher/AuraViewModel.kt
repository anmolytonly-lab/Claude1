package com.auraos.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auraos.launcher.ai.ActionExecutor
import com.auraos.launcher.ai.ActionResult
import com.auraos.launcher.ai.ConversationMemory
import com.auraos.launcher.ai.GeminiApiClient
import com.auraos.launcher.ai.IntentResponse
import com.auraos.launcher.voice.VoiceRecognitionManager
import com.auraos.launcher.voice.VoiceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val isTyping: Boolean = false,
    val isError: Boolean = false,
    val isActionCard: Boolean = false,
    val pendingAction: IntentResponse? = null,
    val displayedText: String = if (isUser) text else ""
)

enum class AssistantState {
    HIDDEN, VISIBLE, LISTENING, THINKING
}

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiClient = GeminiApiClient()
    private val actionExecutor = ActionExecutor(application)
    private val conversationMemory = ConversationMemory()
    val voiceManager = VoiceRecognitionManager(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _assistantState = MutableStateFlow(AssistantState.HIDDEN)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(false)
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    init {
        voiceManager.onResult = { text ->
            if (text.isNotBlank()) {
                _inputText.value = text
                sendMessage(text)
            }
            _assistantState.value = AssistantState.VISIBLE
        }
        voiceManager.onError = { error ->
            if (_assistantState.value == AssistantState.LISTENING) {
                _assistantState.value = AssistantState.VISIBLE
            }
        }
    }

    fun showAssistant() {
        _assistantState.value = AssistantState.VISIBLE
        if (_messages.value.isEmpty()) {
            _messages.value = listOf(
                ChatMessage(
                    text = "Hi! I'm Aura, your AI assistant. How can I help you today?",
                    isUser = false,
                    displayedText = "Hi! I'm Aura, your AI assistant. How can I help you today?"
                )
            )
        }
    }

    fun hideAssistant() {
        _assistantState.value = AssistantState.HIDDEN
        voiceManager.stopListening()
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage(text: String? = null) {
        val message = text ?: _inputText.value
        if (message.isBlank()) return

        _inputText.value = ""
        voiceManager.stopListening()

        val userMsg = ChatMessage(text = message, isUser = true, displayedText = message)
        val thinkingMsg = ChatMessage(text = "", isUser = false, isTyping = true)

        _messages.value = _messages.value + userMsg + thinkingMsg
        _assistantState.value = AssistantState.THINKING

        conversationMemory.addUserMessage(message)

        viewModelScope.launch {
            val response = geminiClient.sendMessage(message, conversationMemory.getHistory())
            conversationMemory.addModelResponse(response.replyText)

            // Remove thinking indicator
            _messages.value = _messages.value.filter { !it.isTyping }

            val result = actionExecutor.execute(response)

            when (result) {
                is ActionResult.NeedsConfirmation -> {
                    val confirmMsg = ChatMessage(
                        text = response.replyText,
                        isUser = false,
                        isActionCard = true,
                        pendingAction = response,
                        displayedText = response.replyText
                    )
                    _messages.value = _messages.value + confirmMsg
                }
                is ActionResult.Info -> {
                    addAuraMessage(result.info)
                }
                is ActionResult.AppOpened -> {
                    addAuraMessage(response.replyText)
                }
                is ActionResult.Error -> {
                    val errorMsg = ChatMessage(
                        text = result.message,
                        isUser = false,
                        isError = true,
                        displayedText = result.message
                    )
                    _messages.value = _messages.value + errorMsg
                }
                is ActionResult.Success -> {
                    addAuraMessage(response.replyText)
                }
            }

            _assistantState.value = AssistantState.VISIBLE
        }
    }

    private fun addAuraMessage(text: String) {
        val msg = ChatMessage(text = text, isUser = false, displayedText = "")
        _messages.value = _messages.value + msg

        // Typing animation — reveal characters progressively
        viewModelScope.launch {
            val msgIndex = _messages.value.indexOfLast { it.id == msg.id }
            if (msgIndex == -1) return@launch

            for (i in 1..text.length) {
                val current = _messages.value.toMutableList()
                if (msgIndex < current.size) {
                    current[msgIndex] = current[msgIndex].copy(displayedText = text.substring(0, i))
                    _messages.value = current
                }
                delay(12L)
            }
        }
    }

    fun confirmAction(response: IntentResponse) {
        viewModelScope.launch {
            val result = actionExecutor.executeConfirmedAction(response)
            // Remove the confirmation card
            _messages.value = _messages.value.map {
                if (it.pendingAction == response) {
                    it.copy(isActionCard = false, pendingAction = null, text = "✅ ${it.text}", displayedText = "✅ ${it.text}")
                } else it
            }
            when (result) {
                is ActionResult.Success -> addAuraMessage(result.message)
                is ActionResult.Error -> addAuraMessage("❌ ${result.message}")
                else -> {}
            }
        }
    }

    fun cancelAction(response: IntentResponse) {
        _messages.value = _messages.value.map {
            if (it.pendingAction == response) {
                it.copy(isActionCard = false, pendingAction = null, text = "❌ Cancelled", displayedText = "❌ Cancelled")
            } else it
        }
    }

    fun startVoiceInput() {
        _assistantState.value = AssistantState.LISTENING
        voiceManager.startListening()
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
        _assistantState.value = AssistantState.VISIBLE
    }

    fun toggleWakeWord(context: android.content.Context) {
        _wakeWordEnabled.value = !_wakeWordEnabled.value
        if (_wakeWordEnabled.value) {
            com.auraos.launcher.voice.WakeWordService.start(context)
        } else {
            com.auraos.launcher.voice.WakeWordService.stop(context)
        }
    }

    fun clearConversation() {
        _messages.value = emptyList()
        conversationMemory.clear()
        showAssistant()
    }

    override fun onCleared() {
        voiceManager.destroy()
        super.onCleared()
    }
}
