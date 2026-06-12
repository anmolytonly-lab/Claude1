package com.aicaller.app.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.aicaller.app.AiCallerApplication
import com.aicaller.app.R
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.worker.SummarizationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that runs Android's on-device [SpeechRecognizer] for
 * the duration of an active call to build a live transcript. The transcript
 * is persisted when the call ends and handed off for AI summarization.
 */
@AndroidEntryPoint
class TranscriptionForegroundService : Service() {

    @Inject lateinit var callRepository: CallRepository

    private var speechRecognizer: SpeechRecognizer? = null
    private val transcriptBuilder = StringBuilder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var callerNumber: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        callerNumber = intent?.getStringExtra(EXTRA_NUMBER)
        startForeground(NOTIFICATION_ID, buildNotification())
        startListening()
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, AiCallerApplication.CHANNEL_TRANSCRIPTION)
            .setContentTitle("AI Caller")
            .setContentText("Transcribing call for AI summary...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { text ->
                        transcriptBuilder.append(text).append(". ")
                    }
                    restartListening()
                }

                override fun onError(error: Int) = restartListening()
                override fun onEndOfSpeech() = restartListening()

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        restartListening()
    }

    private fun restartListening() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(recognizerIntent)
        } catch (_: Exception) {
            // Recognizer may be transiently busy between restarts; safe to ignore.
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()

        val transcript = transcriptBuilder.toString().trim()
        val number = callerNumber
        if (transcript.isNotBlank() && number != null) {
            scope.launch {
                callRepository.syncSystemCallLog()
                val latest = callRepository.latestRecordForNumber(number)
                if (latest != null) {
                    callRepository.saveTranscript(latest.id, transcript)
                    SummarizationWorker.enqueue(applicationContext)
                }
            }
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val EXTRA_NUMBER = "extra_number"

        fun start(context: Context, number: String?) {
            val intent = Intent(context, TranscriptionForegroundService::class.java)
                .putExtra(EXTRA_NUMBER, number)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TranscriptionForegroundService::class.java))
        }
    }
}
