package com.aicaller.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SpamSource { USER_REPORTED, AI_DETECTED, COMMUNITY }

@Entity(tableName = "spam_numbers")
data class SpamNumberEntity(
    @PrimaryKey val phoneNumber: String,
    val riskScore: Int,          // 0..100, higher = more likely spam
    val reason: String,
    val source: SpamSource,
    val reportedAt: Long = System.currentTimeMillis(),
    val autoBlock: Boolean = false
)
