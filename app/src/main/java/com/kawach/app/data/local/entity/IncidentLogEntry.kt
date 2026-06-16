package com.kawach.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An entry in the private incident log.
 * [triggeredBySOS] marks entries auto-created by the SOS panic button.
 */
@Entity(tableName = "incident_log")
data class IncidentLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,                 // System.currentTimeMillis()
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String,
    val photoUri: String? = null,        // content:// URI string from gallery/CameraX
    val triggeredBySOS: Boolean = false
)
