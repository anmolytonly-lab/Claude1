package com.kawach.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kawach.app.data.datastore.SettingsDataStore
import com.kawach.app.data.local.AppDatabase
import com.kawach.app.data.repository.ContactRepository
import com.kawach.app.data.repository.IncidentRepository
import com.kawach.app.util.SosManager

/** Manual dependency injection — holds all singletons. */
class KawachApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val contactRepository by lazy { ContactRepository(database.emergencyContactDao()) }
    val incidentRepository by lazy { IncidentRepository(database.incidentLogDao()) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
    val sosManager by lazy { SosManager(this, incidentRepository) }

    companion object {
        const val CHANNEL_WALKING_TIMER = "walking_timer_channel"
        const val CHANNEL_SOS = "sos_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_WALKING_TIMER,
                    getString(R.string.channel_walking_timer),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Shows countdown for Walking Home timer" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SOS,
                    getString(R.string.notif_channel_sos),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "SOS activation alerts" }
            )
        }
    }
}
