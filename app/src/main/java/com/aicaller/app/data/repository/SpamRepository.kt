package com.aicaller.app.data.repository

import com.aicaller.app.ai.AiClient
import com.aicaller.app.ai.SpamAnalysisResult
import com.aicaller.app.ai.SpamHeuristics
import com.aicaller.app.data.local.dao.CallRecordDao
import com.aicaller.app.data.local.dao.SpamNumberDao
import com.aicaller.app.data.local.entities.SpamNumberEntity
import com.aicaller.app.data.local.entities.SpamSource
import com.aicaller.app.util.PhoneNumberUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpamRepository @Inject constructor(
    private val spamDao: SpamNumberDao,
    private val callRecordDao: CallRecordDao,
    private val aiClient: AiClient
) {

    fun observeSpamList(): Flow<List<SpamNumberEntity>> = spamDao.observeAll()

    /**
     * Returns a risk assessment for [phoneNumber], preferring a cached
     * verdict, then a fast local heuristic, then (if available) an AI call.
     */
    suspend fun assessNumber(phoneNumber: String, useAi: Boolean = true): SpamAnalysisResult {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)

        spamDao.getByNumber(normalized)?.let { cached ->
            return SpamAnalysisResult(
                riskScore = cached.riskScore,
                label = if (cached.riskScore >= 70) "Likely spam" else "Suspicious",
                reason = cached.reason,
                shouldBlock = cached.autoBlock,
                shouldScreen = cached.riskScore >= 35
            )
        }

        val heuristic = SpamHeuristics.score(normalized)
        if (!useAi) return heuristic

        val history = callRecordDao.recentForNumber(normalized, limit = 5)
        val recentTranscript = history.firstOrNull()?.transcript?.take(280)
        val context = buildString {
            if (history.isNotEmpty()) {
                append("This number has called ${history.size} time(s) before. ")
            }
            recentTranscript?.let { append("Recent call transcript snippet: \"$it\".") }
        }.trim().ifBlank { null }

        val result = if (heuristic.riskScore in 1..99) {
            aiClient.analyzeSpamRisk(normalized, context)
        } else {
            heuristic
        }

        if (result.riskScore >= 50) {
            spamDao.upsert(
                SpamNumberEntity(
                    phoneNumber = normalized,
                    riskScore = result.riskScore,
                    reason = result.reason,
                    source = SpamSource.AI_DETECTED,
                    autoBlock = result.shouldBlock
                )
            )
        }

        return result
    }

    suspend fun reportSpam(phoneNumber: String, reason: String) {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        spamDao.upsert(
            SpamNumberEntity(
                phoneNumber = normalized,
                riskScore = 100,
                reason = reason,
                source = SpamSource.USER_REPORTED,
                autoBlock = true
            )
        )
    }

    suspend fun unblock(phoneNumber: String) {
        spamDao.delete(PhoneNumberUtils.normalize(phoneNumber))
    }
}
