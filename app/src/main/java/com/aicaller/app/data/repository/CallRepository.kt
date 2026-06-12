package com.aicaller.app.data.repository

import android.content.Context
import android.provider.CallLog
import com.aicaller.app.ai.AiClient
import com.aicaller.app.data.local.dao.CallRecordDao
import com.aicaller.app.data.local.entities.CallDirection
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.local.entities.ContactInsightEntity
import com.aicaller.app.util.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callRecordDao: CallRecordDao,
    private val contactRepository: ContactRepository,
    private val aiClient: AiClient
) {

    fun observeCallRecords(): Flow<List<CallRecordEntity>> = callRecordDao.observeAll()

    fun observeCallRecordsForNumber(phoneNumber: String): Flow<List<CallRecordEntity>> =
        callRecordDao.observeForNumber(PhoneNumberUtils.normalize(phoneNumber))

    suspend fun getRecord(id: Long): CallRecordEntity? = callRecordDao.getById(id)

    suspend fun latestRecordForNumber(phoneNumber: String): CallRecordEntity? =
        callRecordDao.recentForNumber(PhoneNumberUtils.normalize(phoneNumber), limit = 1).firstOrNull()

    /**
     * Pulls recent entries from the system [CallLog] that we haven't imported
     * yet and stores a corresponding [CallRecordEntity]. Requires READ_CALL_LOG.
     */
    suspend fun syncSystemCallLog(limit: Int = 200) = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            ),
            null, null,
            "${CallLog.Calls.DATE} DESC LIMIT $limit"
        )

        cursor?.use {
            val idIdx = it.getColumnIndex(CallLog.Calls._ID)
            val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val systemId = it.getLong(idIdx)
                val number = it.getString(numberIdx) ?: continue
                val direction = when (it.getInt(typeIdx)) {
                    CallLog.Calls.OUTGOING_TYPE -> CallDirection.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallDirection.MISSED
                    CallLog.Calls.REJECTED_TYPE, CallLog.Calls.BLOCKED_TYPE -> CallDirection.BLOCKED
                    else -> CallDirection.INCOMING
                }

                val existing = callRecordDao.recentForNumber(PhoneNumberUtils.normalize(number), limit = 50)
                    .firstOrNull { it.systemCallLogId == systemId }

                if (existing == null) {
                    callRecordDao.insert(
                        CallRecordEntity(
                            systemCallLogId = systemId,
                            phoneNumber = PhoneNumberUtils.normalize(number),
                            contactName = it.getString(nameIdx),
                            direction = direction,
                            startedAt = it.getLong(dateIdx),
                            durationSeconds = it.getInt(durationIdx)
                        )
                    )
                }
            }
        }
    }

    /** Attaches a freshly captured transcript to a call record. */
    suspend fun saveTranscript(callRecordId: Long, transcript: String, recordingPath: String? = null) {
        val record = callRecordDao.getById(callRecordId) ?: return
        callRecordDao.update(record.copy(transcript = transcript, recordingPath = recordingPath ?: record.recordingPath))
    }

    /**
     * Generates an AI summary, action items and sentiment for the next call
     * record that has a transcript but no summary yet, and folds the result
     * into that contact's running insights.
     */
    suspend fun summarizeNextPendingCall(): CallRecordEntity? {
        val record = callRecordDao.getNextPendingSummary() ?: return null
        val transcript = record.transcript ?: return null
        val contactName = record.contactName ?: contactRepository.lookupNameByNumber(record.phoneNumber)

        val result = aiClient.summarizeCall(transcript, contactName)

        val updated = record.copy(
            summary = result.summary,
            actionItems = result.actionItems.joinToString("\n"),
            sentiment = result.sentiment
        )
        callRecordDao.update(updated)

        val existingInsight = contactRepository.getInsight(record.phoneNumber)
        val totalCalls = (existingInsight?.totalCalls ?: 0) + 1
        val previousAvg = existingInsight?.averageSentiment ?: 0f
        val newAvg = ((previousAvg * (totalCalls - 1)) + result.sentiment) / totalCalls
        val mergedTags = (existingInsight?.tags?.split(",")?.filter { it.isNotBlank() }.orEmpty() + result.suggestedTags)
            .distinct()
            .joinToString(",")

        contactRepository.upsertInsight(
            ContactInsightEntity(
                phoneNumber = record.phoneNumber,
                displayName = contactName,
                tags = mergedTags,
                relationship = result.relationship ?: existingInsight?.relationship,
                averageSentiment = newAvg,
                totalCalls = totalCalls,
                suggestedCallWindow = existingInsight?.suggestedCallWindow,
                lastSummary = result.summary
            )
        )

        return updated
    }
}
