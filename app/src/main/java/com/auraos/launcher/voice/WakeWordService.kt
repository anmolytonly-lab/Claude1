package com.auraos.launcher.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.auraos.launcher.R

class WakeWordService : Service() {

    companion object {
        const val CHANNEL_ID = "aura_wake_word"
        const val NOTIFICATION_ID = 1001
        const val ACTION_WAKE_DETECTED = "com.auraos.launcher.WAKE_DETECTED"
        private const val TAG = "WakeWordService"
        private val WAKE_PHRASES = listOf("hey aura", "hi aura", "aura", "hey ora", "a aura")

        fun start(context: Context) {
            val intent = Intent(context, WakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WakeWordService::class.java))
        }
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWakeWordDetection()
        return START_STICKY
    }

    override fun onDestroy() {
        stopWakeWordDetection()
        super.onDestroy()
    }

    private fun startWakeWordDetection() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.w(TAG, "Speech recognition not available")
            stopSelf()
            return
        }

        startListeningCycle()
    }

    private fun startListeningCycle() {
        if (isListening) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                    // Restart listening after a short delay on recoverable errors
                    if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        android.os.Handler(mainLooper).postDelayed({ startListeningCycle() }, 1000)
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    checkForWakeWord(matches)
                    // Restart listening for next wake word
                    android.os.Handler(mainLooper).postDelayed({ startListeningCycle() }, 500)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    checkForWakeWord(matches)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            android.os.Handler(mainLooper).postDelayed({ startListeningCycle() }, 2000)
        }
    }

    private fun checkForWakeWord(matches: List<String>?) {
        matches?.forEach { text ->
            val lower = text.lowercase().trim()
            if (WAKE_PHRASES.any { phrase -> lower.contains(phrase) }) {
                Log.d(TAG, "Wake word detected: $text")
                broadcastWakeDetected()
            }
        }
    }

    private fun broadcastWakeDetected() {
        val intent = Intent(ACTION_WAKE_DETECTED).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun stopWakeWordDetection() {
        isListening = false
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Wake Word",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Listening for \"Hey Aura\" wake word"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aura is listening")
            .setContentText("Say \"Hey Aura\" to activate")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
