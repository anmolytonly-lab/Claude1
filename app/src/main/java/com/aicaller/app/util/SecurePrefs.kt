package com.aicaller.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Encrypted storage for the user's AI API key and feature toggles. */
@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "ai_caller_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var geminiApiKey: String?
        get() = prefs.getString(KEY_GEMINI_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_GEMINI_API_KEY, value).apply()

    var spamScreeningEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPAM_SCREENING, true)
        set(value) = prefs.edit().putBoolean(KEY_SPAM_SCREENING, value).apply()

    var liveTranscriptionEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRANSCRIPTION, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSCRIPTION, value).apply()

    var smartAutoReplyEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_REPLY, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_REPLY, value).apply()

    var autoBlockHighRiskCalls: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BLOCK, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BLOCK, value).apply()

    /** Whether quiet hours (do-not-disturb scheduling) are enabled. */
    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, value).apply()

    /** Quiet hours start, as hour-of-day (0-23). */
    var quietHoursStart: Int
        get() = prefs.getInt(KEY_QUIET_HOURS_START, 22)
        set(value) = prefs.edit().putInt(KEY_QUIET_HOURS_START, value).apply()

    /** Quiet hours end, as hour-of-day (0-23). May be less than [quietHoursStart], wrapping past midnight. */
    var quietHoursEnd: Int
        get() = prefs.getInt(KEY_QUIET_HOURS_END, 7)
        set(value) = prefs.edit().putInt(KEY_QUIET_HOURS_END, value).apply()

    /** Custom auto-reply message sent during quiet hours, instead of the AI-generated one. */
    var quietHoursMessage: String
        get() = prefs.getString(KEY_QUIET_HOURS_MESSAGE, DEFAULT_QUIET_HOURS_MESSAGE) ?: DEFAULT_QUIET_HOURS_MESSAGE
        set(value) = prefs.edit().putString(KEY_QUIET_HOURS_MESSAGE, value).apply()

    /** Whether unknown numbers should be silenced (sent to voicemail) during quiet hours. */
    var quietHoursSilenceUnknown: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS_SILENCE_UNKNOWN, true)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS_SILENCE_UNKNOWN, value).apply()

    /** Checks whether the current time falls within the configured quiet hours window. */
    fun isWithinQuietHours(hourOfDay: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)): Boolean {
        if (!quietHoursEnabled) return false
        val start = quietHoursStart
        val end = quietHoursEnd
        return if (start == end) {
            false
        } else if (start < end) {
            hourOfDay in start until end
        } else {
            hourOfDay >= start || hourOfDay < end
        }
    }

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_SPAM_SCREENING = "feature_spam_screening"
        private const val KEY_TRANSCRIPTION = "feature_transcription"
        private const val KEY_AUTO_REPLY = "feature_auto_reply"
        private const val KEY_AUTO_BLOCK = "feature_auto_block"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"
        private const val KEY_QUIET_HOURS_MESSAGE = "quiet_hours_message"
        private const val KEY_QUIET_HOURS_SILENCE_UNKNOWN = "quiet_hours_silence_unknown"
        const val DEFAULT_QUIET_HOURS_MESSAGE =
            "Thanks for calling. I'm currently unavailable (quiet hours) and will get back to you soon."
    }
}
