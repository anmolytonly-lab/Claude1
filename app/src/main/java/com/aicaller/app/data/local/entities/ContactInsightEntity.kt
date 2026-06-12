package com.aicaller.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI-derived metadata about a contact, keyed by normalized phone number.
 * Layered on top of the device contact book rather than replacing it.
 */
@Entity(tableName = "contact_insights")
data class ContactInsightEntity(
    @PrimaryKey val phoneNumber: String,
    val displayName: String? = null,
    val tags: String = "",                 // comma-separated AI-suggested tags, e.g. "work,recruiter"
    val notes: String = "",
    val relationship: String? = null,       // e.g. "family", "colleague", "delivery"
    val averageSentiment: Float = 0f,       // -1.0 (negative) .. 1.0 (positive)
    val totalCalls: Int = 0,
    val suggestedCallWindow: String? = null,// e.g. "Weekdays 6-8 PM"
    val lastSummary: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
