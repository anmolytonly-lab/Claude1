package com.aicaller.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicaller.app.data.local.entities.ContactInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactInsightDao {

    @Query("SELECT * FROM contact_insights ORDER BY lastUpdated DESC")
    fun observeAll(): Flow<List<ContactInsightEntity>>

    @Query("SELECT * FROM contact_insights WHERE phoneNumber = :phoneNumber")
    suspend fun getByNumber(phoneNumber: String): ContactInsightEntity?

    @Query("SELECT * FROM contact_insights WHERE phoneNumber = :phoneNumber")
    fun observeByNumber(phoneNumber: String): Flow<ContactInsightEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(insight: ContactInsightEntity)
}
