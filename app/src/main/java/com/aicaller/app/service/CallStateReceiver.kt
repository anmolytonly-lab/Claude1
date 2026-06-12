package com.aicaller.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.aicaller.app.util.SecurePrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Tracks phone call state transitions (idle -> ringing -> offhook -> idle)
 * to drive live transcription and missed-call auto-reply.
 */
@AndroidEntryPoint
class CallStateReceiver : BroadcastReceiver() {

    @Inject lateinit var securePrefs: SecurePrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        val previousState = lastState
        lastState = state

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                if (!incomingNumber.isNullOrBlank()) {
                    lastRingingNumber = incomingNumber
                }
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (securePrefs.liveTranscriptionEnabled) {
                    TranscriptionForegroundService.start(context, lastRingingNumber)
                }
                wasAnswered = true
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (wasAnswered) {
                    TranscriptionForegroundService.stop(context)
                } else if (previousState == TelephonyManager.EXTRA_STATE_RINGING) {
                    // Ringing -> idle without going off-hook == missed/rejected call.
                    lastRingingNumber?.let { number ->
                        if (securePrefs.smartAutoReplyEnabled) {
                            MissedCallReceiver.notifyMissedCall(context, number)
                        }
                    }
                }
                wasAnswered = false
                lastRingingNumber = null
            }
        }
    }

    companion object {
        @Volatile private var lastState: String? = null
        @Volatile private var lastRingingNumber: String? = null
        @Volatile private var wasAnswered: Boolean = false
    }
}
