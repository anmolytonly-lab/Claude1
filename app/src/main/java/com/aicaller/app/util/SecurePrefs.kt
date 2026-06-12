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

    var anthropicApiKey: String?
        get() = prefs.getString(KEY_ANTHROPIC_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_ANTHROPIC_API_KEY, value).apply()

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

    companion object {
        private const val KEY_ANTHROPIC_API_KEY = "anthropic_api_key"
        private const val KEY_SPAM_SCREENING = "feature_spam_screening"
        private const val KEY_TRANSCRIPTION = "feature_transcription"
        private const val KEY_AUTO_REPLY = "feature_auto_reply"
        private const val KEY_AUTO_BLOCK = "feature_auto_block"
    }
}
