package com.kawach.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kawach.app.data.local.dao.EmergencyContactDao
import com.kawach.app.data.local.dao.IncidentLogDao
import com.kawach.app.data.local.entity.EmergencyContact
import com.kawach.app.data.local.entity.IncidentLogEntry

@Database(
    entities = [EmergencyContact::class, IncidentLogEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun incidentLogDao(): IncidentLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kawach_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
