package com.aicaller.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aicaller.app.data.repository.AutoReplyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives an internal broadcast when a call rings out without being
 * answered, and triggers an AI-drafted SMS auto-reply.
 */
@AndroidEntryPoint
class MissedCallReceiver : BroadcastReceiver() {

    @Inject lateinit var autoReplyRepository: AutoReplyRepository

    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                autoReplyRepository.sendAutoReplyForMissedCall(number)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"

        fun notifyMissedCall(context: Context, phoneNumber: String) {
            val intent = Intent(context, MissedCallReceiver::class.java)
                .putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            context.sendBroadcast(intent)
        }
    }
}
