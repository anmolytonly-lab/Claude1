package com.aicaller.app.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.aicaller.app.data.repository.SpamRepository
import com.aicaller.app.util.SecurePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * AI-powered call screening. The system calls [onScreenCall] for every
 * incoming call and we must respond within a few seconds, so the AI/spam
 * lookup is bounded by [SCREENING_TIMEOUT_MS] with a safe "allow" default.
 */
@AndroidEntryPoint
class CallScreeningServiceImpl : CallScreeningService() {

    @Inject lateinit var spamRepository: SpamRepository
    @Inject lateinit var securePrefs: SecurePrefs

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart

        if (number.isNullOrBlank() || !securePrefs.spamScreeningEnabled) {
            respondToCall(callDetails, allowResponse())
            return
        }

        val assessment = runBlocking {
            withTimeoutOrNull(SCREENING_TIMEOUT_MS) {
                spamRepository.assessNumber(number, useAi = true)
            }
        }

        val response = when {
            assessment == null -> allowResponse()
            assessment.shouldBlock && securePrefs.autoBlockHighRiskCalls -> blockResponse()
            assessment.shouldScreen -> silenceResponse()
            else -> allowResponse()
        }

        respondToCall(callDetails, response)
    }

    private fun allowResponse() = CallResponse.Builder()
        .setDisallowCall(false)
        .setRejectCall(false)
        .setSkipCallLog(false)
        .setSkipNotification(false)
        .build()

    private fun blockResponse() = CallResponse.Builder()
        .setDisallowCall(true)
        .setRejectCall(true)
        .setSkipCallLog(false)
        .setSkipNotification(true)
        .build()

    /** Lets the call through but suppresses ringing/notification - useful for "likely spam, but not certain". */
    private fun silenceResponse() = CallResponse.Builder()
        .setDisallowCall(false)
        .setRejectCall(false)
        .setSilenceCall(true)
        .setSkipCallLog(false)
        .setSkipNotification(true)
        .build()

    companion object {
        private const val SCREENING_TIMEOUT_MS = 4000L
    }
}
