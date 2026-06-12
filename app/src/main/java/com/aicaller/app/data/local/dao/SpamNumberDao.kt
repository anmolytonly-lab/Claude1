package com.aicaller.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicaller.app.data.local.entities.SpamNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamNumberDao {

    @Query("SELECT * FROM spam_numbers ORDER BY reportedAt DESC")
    fun observeAll(): Flow<List<SpamNumberEntity>>

    @Query("SELECT * FROM spam_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun getByNumber(phoneNumber: String): SpamNumberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SpamNumberEntity)

    @Query("DELETE FROM spam_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun delete(phoneNumber: String)
}
