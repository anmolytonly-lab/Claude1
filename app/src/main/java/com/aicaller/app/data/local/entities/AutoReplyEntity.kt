package com.aicaller.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Log of AI-generated auto-replies sent for missed calls. */
@Entity(tableName = "auto_replies")
data class AutoReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val sentAt: Long = System.currentTimeMillis(),
    val triggeredBySpam: Boolean = false
)
