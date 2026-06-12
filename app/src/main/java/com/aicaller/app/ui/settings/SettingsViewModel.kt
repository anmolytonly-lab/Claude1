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

    var aiModel by mutableStateOf(securePrefs.aiModel)
        private set

    fun onAiModelChanged(model: String) {
        aiModel = model
        securePrefs.aiModel = model
    }

    var spamScreeningEnabled by mutableStateOf(securePrefs.spamScreeningEnabled)
        private set

    var liveTranscriptionEnabled by mutableStateOf(securePrefs.liveTranscriptionEnabled)
        private set

    var smartAutoReplyEnabled by mutableStateOf(securePrefs.smartAutoReplyEnabled)
        private set

    var autoBlockHighRiskCalls by mutableStateOf(securePrefs.autoBlockHighRiskCalls)
        private set

    var quietHoursEnabled by mutableStateOf(securePrefs.quietHoursEnabled)
        private set

    var quietHoursStart by mutableStateOf(securePrefs.quietHoursStart)
        private set

    var quietHoursEnd by mutableStateOf(securePrefs.quietHoursEnd)
        private set

    var quietHoursMessage by mutableStateOf(securePrefs.quietHoursMessage)
        private set

    var quietHoursSilenceUnknown by mutableStateOf(securePrefs.quietHoursSilenceUnknown)
        private set

    fun onQuietHoursEnabledChanged(enabled: Boolean) {
        quietHoursEnabled = enabled
        securePrefs.quietHoursEnabled = enabled
    }

    fun onQuietHoursStartChanged(hour: Int) {
        quietHoursStart = hour
        securePrefs.quietHoursStart = hour
    }

    fun onQuietHoursEndChanged(hour: Int) {
        quietHoursEnd = hour
        securePrefs.quietHoursEnd = hour
    }

    fun onQuietHoursMessageChanged(message: String) {
        quietHoursMessage = message
        securePrefs.quietHoursMessage = message
    }

    fun onQuietHoursSilenceUnknownChanged(enabled: Boolean) {
        quietHoursSilenceUnknown = enabled
        securePrefs.quietHoursSilenceUnknown = enabled
    }

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
