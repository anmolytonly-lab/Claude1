package com.kawach.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a trusted contact (personal emergency or neighbourhood group member).
 * [isNeighbourhoodGroup] = false  →  personal emergency contact (SOS SMS recipient)
 * [isNeighbourhoodGroup] = true   →  community alert group member
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isNeighbourhoodGroup: Boolean = false
)
