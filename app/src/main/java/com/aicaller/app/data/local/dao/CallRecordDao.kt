package com.aicaller.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aicaller.app.data.local.entities.CallRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {

    @Query("SELECT * FROM call_records ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE id = :id")
    suspend fun getById(id: Long): CallRecordEntity?

    @Query("SELECT * FROM call_records WHERE phoneNumber = :phoneNumber ORDER BY startedAt DESC")
    fun observeForNumber(phoneNumber: String): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE transcript IS NOT NULL AND summary IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getNextPendingSummary(): CallRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CallRecordEntity): Long

    @Update
    suspend fun update(record: CallRecordEntity)

    @Query("SELECT * FROM call_records WHERE phoneNumber = :phoneNumber ORDER BY startedAt DESC LIMIT :limit")
    suspend fun recentForNumber(phoneNumber: String, limit: Int = 10): List<CallRecordEntity>
}
