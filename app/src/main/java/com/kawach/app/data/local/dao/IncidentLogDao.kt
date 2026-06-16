package com.kawach.app.data.local.dao

import androidx.room.*
import com.kawach.app.data.local.entity.IncidentLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentLogDao {

    @Query("SELECT * FROM incident_log ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<IncidentLogEntry>>

    @Query("SELECT * FROM incident_log WHERE id = :id")
    suspend fun getById(id: Int): IncidentLogEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: IncidentLogEntry): Long

    @Update
    suspend fun update(entry: IncidentLogEntry)

    @Delete
    suspend fun delete(entry: IncidentLogEntry)

    @Query("DELETE FROM incident_log")
    suspend fun deleteAll()
}
