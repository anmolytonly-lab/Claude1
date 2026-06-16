package com.kawach.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kawach.app.KawachApp
import com.kawach.app.MainActivity
import com.kawach.app.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service that counts down the Walking Home timer.
 * If the countdown reaches zero the SOS flow fires automatically.
 *
 * Start it with [ACTION_START] + extra [EXTRA_DURATION_SECONDS].
 * Cancel safely with [ACTION_CANCEL].
 */
class WalkingTimerService : Service() {

    companion object {
        const val ACTION_START = "com.kawach.app.WALKING_TIMER_START"
        const val ACTION_CANCEL = "com.kawach.app.WALKING_TIMER_CANCEL"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val NOTIFICATION_ID = 1001

        // Exposed so the UI can observe without binding
        private val _remainingSeconds = MutableStateFlow(0L)
        val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> cancelTimer()
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_SECONDS, 600L)
                startTimer(duration)
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationSeconds: Long) {
        startForeground(NOTIFICATION_ID, buildNotification(durationSeconds))
        _isRunning.value = true

        timerJob = scope.launch {
            var remaining = durationSeconds
            while (remaining > 0 && isActive) {
                _remainingSeconds.value = remaining
                updateNotification(remaining)
                delay(1000L)
                remaining--
            }
            if (remaining <= 0) {
                // Timer expired — fire SOS
                triggerSos()
            }
            stopSelf()
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        _isRunning.value = false
        _remainingSeconds.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun triggerSos() {
        val app = application as KawachApp
        // Collect personal contacts once
        var contacts = emptyList<com.kawach.app.data.local.entity.EmergencyContact>()
        val collectJob = scope.launch {
            app.contactRepository.personalContacts.collect { list ->
                contacts = list
                cancel()
            }
        }
        collectJob.join()
        app.sosManager.activate(contacts, scope)
    }

    private fun buildNotification(remainingSeconds: Long): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val cancelIntent = PendingIntent.getService(
            this, 1,
            Intent(this, WalkingTimerService::class.java).apply { action = ACTION_CANCEL },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, KawachApp.CHANNEL_WALKING_TIMER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notif_walking_title))
            .setContentText(formatTime(remainingSeconds))
            .setContentIntent(openAppIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.notif_im_safe),
                cancelIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(remaining: Long) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(remaining))
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d बाकी है — Timer चल रहा है".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        _isRunning.value = false
    }
}
