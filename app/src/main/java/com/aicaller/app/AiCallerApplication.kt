package com.aicaller.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AiCallerApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_TRANSCRIPTION,
                    "Live transcription",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shown while AI Caller is transcribing an active call"
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CALL_INSIGHTS,
                    "Call insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "AI summaries, spam alerts and auto-reply notifications"
                }
            )
        }
    }

    companion object {
        const val CHANNEL_TRANSCRIPTION = "transcription_channel"
        const val CHANNEL_CALL_INSIGHTS = "call_insights_channel"
    }
}
