package com.aicaller.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aicaller.app.util.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePrefs
) : ViewModel() {

    var apiKey by mutableStateOf(securePrefs.geminiApiKey ?: "")
        private set

    var spamScreeningEnabled by mutableStateOf(securePrefs.spamScreeningEnabled)
        private set

    var liveTranscriptionEnabled by mutableStateOf(securePrefs.liveTranscriptionEnabled)
        private set

    var smartAutoReplyEnabled by mutableStateOf(securePrefs.smartAutoReplyEnabled)
        private set

    var autoBlockHighRiskCalls by mutableStateOf(securePrefs.autoBlockHighRiskCalls)
        private set

    fun onApiKeyChanged(value: String) {
        apiKey = value
        securePrefs.geminiApiKey = value.trim().ifBlank { null }
    }

    fun onSpamScreeningChanged(enabled: Boolean) {
        spamScreeningEnabled = enabled
        securePrefs.spamScreeningEnabled = enabled
    }

    fun onLiveTranscriptionChanged(enabled: Boolean) {
        liveTranscriptionEnabled = enabled
        securePrefs.liveTranscriptionEnabled = enabled
    }

    fun onSmartAutoReplyChanged(enabled: Boolean) {
        smartAutoReplyEnabled = enabled
        securePrefs.smartAutoReplyEnabled = enabled
    }

    fun onAutoBlockChanged(enabled: Boolean) {
        autoBlockHighRiskCalls = enabled
        securePrefs.autoBlockHighRiskCalls = enabled
    }
}
