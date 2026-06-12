package com.aicaller.app.data.repository

import android.content.Context
import android.telephony.SmsManager
import com.aicaller.app.ai.AiClient
import com.aicaller.app.ai.AutoReplyContext
import com.aicaller.app.ai.LocalHeuristicAiClient
import com.aicaller.app.data.local.dao.AutoReplyDao
import com.aicaller.app.data.local.entities.AutoReplyEntity
import com.aicaller.app.util.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoReplyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val autoReplyDao: AutoReplyDao,
    private val aiClient: AiClient,
    private val contactRepository: ContactRepository,
    private val spamRepository: SpamRepository
) {

    fun observeHistory(): Flow<List<AutoReplyEntity>> = autoReplyDao.observeAll()

    /**
     * Generates a context-aware SMS for a missed call and sends it.
     * Requires SEND_SMS permission.
     */
    suspend fun sendAutoReplyForMissedCall(phoneNumber: String) {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        val insight = contactRepository.getInsight(normalized)
        val callerName = insight?.displayName ?: contactRepository.lookupNameByNumber(normalized)
        val spamAssessment = spamRepository.assessNumber(normalized, useAi = false)

        val message = aiClient.generateAutoReply(
            AutoReplyContext(
                callerNumber = normalized,
                callerName = callerName,
                relationship = insight?.relationship,
                timeOfDay = LocalHeuristicAiClient.timeOfDayLabel(),
                isLikelySpam = spamAssessment.shouldScreen,
                recentSummary = insight?.lastSummary
            )
        )

        val smsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(normalized, null, message, null, null)

        autoReplyDao.insert(
            AutoReplyEntity(
                phoneNumber = normalized,
                message = message,
                triggeredBySpam = spamAssessment.shouldScreen
            )
        )
    }
}
