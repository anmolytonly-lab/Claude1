package com.aicaller.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aicaller.app.data.local.entities.AutoReplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoReplyDao {

    @Query("SELECT * FROM auto_replies ORDER BY sentAt DESC")
    fun observeAll(): Flow<List<AutoReplyEntity>>

    @Insert
    suspend fun insert(entry: AutoReplyEntity)
}
