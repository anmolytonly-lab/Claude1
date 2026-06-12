package com.aicaller.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallDirection { INCOMING, OUTGOING, MISSED, BLOCKED }

/**
 * AI Caller's own record of a call, enriched with transcript/summary data.
 * Linked to the system CallLog via [systemCallLogId] where available.
 */
@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemCallLogId: Long? = null,
    val phoneNumber: String,
    val contactName: String? = null,
    val direction: CallDirection,
    val startedAt: Long,
    val durationSeconds: Int = 0,
    val transcript: String? = null,
    val summary: String? = null,
    val actionItems: String? = null,        // newline-separated
    val sentiment: Float? = null,           // -1.0 .. 1.0
    val spamScore: Int? = null,             // 0..100
    val wasScreened: Boolean = false,
    val recordingPath: String? = null
)
