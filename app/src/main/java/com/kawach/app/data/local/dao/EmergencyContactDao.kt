package com.kawach.app.data.local.dao

import androidx.room.*
import com.kawach.app.data.local.entity.EmergencyContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts WHERE isNeighbourhoodGroup = 0 ORDER BY name ASC")
    fun getPersonalContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE isNeighbourhoodGroup = 1 ORDER BY name ASC")
    fun getNeighbourhoodContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: EmergencyContact)

    @Update
    suspend fun update(contact: EmergencyContact)

    @Delete
    suspend fun delete(contact: EmergencyContact)

    @Query("SELECT COUNT(*) FROM emergency_contacts WHERE isNeighbourhoodGroup = 0")
    fun getPersonalContactCount(): Flow<Int>
}
