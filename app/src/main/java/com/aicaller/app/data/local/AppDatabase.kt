package com.aicaller.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aicaller.app.data.local.dao.AutoReplyDao
import com.aicaller.app.data.local.dao.CallRecordDao
import com.aicaller.app.data.local.dao.ContactInsightDao
import com.aicaller.app.data.local.dao.SpamNumberDao
import com.aicaller.app.data.local.entities.AutoReplyEntity
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.local.entities.ContactInsightEntity
import com.aicaller.app.data.local.entities.SpamNumberEntity

@Database(
    entities = [
        CallRecordEntity::class,
        ContactInsightEntity::class,
        SpamNumberEntity::class,
        AutoReplyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    abstract fun contactInsightDao(): ContactInsightDao
    abstract fun spamNumberDao(): SpamNumberDao
    abstract fun autoReplyDao(): AutoReplyDao

    companion object {
        const val NAME = "ai_caller.db"
    }
}
