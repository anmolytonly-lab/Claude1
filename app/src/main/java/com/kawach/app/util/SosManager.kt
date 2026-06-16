package com.kawach.app.util

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.kawach.app.data.local.entity.EmergencyContact
import com.kawach.app.data.local.entity.IncidentLogEntry
import com.kawach.app.data.repository.IncidentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Orchestrates the full SOS flow:
 *  1. Get GPS location (on-demand — no background polling)
 *  2. Send SMS to all personal emergency contacts
 *  3. Play siren at maximum volume (overrides silent/vibrate mode)
 *  4. Strobe the camera torch
 *  5. Vibrate in alarm pattern
 *  6. Auto-log the incident to the local Room database
 */
class SosManager(
    private val context: Context,
    private val incidentRepository: IncidentRepository
) {

    private var sirenPlayer: MediaPlayer? = null
    private var torchJob: Job? = null
    private var cameraId: String? = null

    // Renamed from isActive to sosRunning to avoid shadowing
    // kotlinx.coroutines.isActive inside coroutine lambdas.
    var sosRunning = false
        private set

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Kick off the full SOS sequence. Must be called from within a coroutine. */
    suspend fun activate(contacts: List<EmergencyContact>, scope: CoroutineScope) {
        if (sosRunning) return
        sosRunning = true

        // ── 1. Location ──────────────────────────────────────────────────────
        var mapsLink = "location unavailable"
        try {
            val location = LocationHelper.getCurrentLocation(context)
            val lat = location?.latitude
            val lon = location?.longitude
            if (lat != null && lon != null) {
                mapsLink = LocationHelper.toMapsLink(lat, lon)
            }
            scope.launch(Dispatchers.IO) {
                incidentRepository.add(
                    IncidentLogEntry(
                        timestamp = System.currentTimeMillis(),
                        latitude = lat,
                        longitude = lon,
                        description = "SOS triggered",
                        triggeredBySOS = true
                    )
                )
            }
        } catch (_: SecurityException) {
            // Location permission denied — continue SOS without a map link
            scope.launch(Dispatchers.IO) {
                incidentRepository.add(
                    IncidentLogEntry(
                        timestamp = System.currentTimeMillis(),
                        description = "SOS triggered (location permission denied)",
                        triggeredBySOS = true
                    )
                )
            }
        }

        // ── 2. SMS ───────────────────────────────────────────────────────────
        if (contacts.isNotEmpty()) {
            val message = context.getString(
                com.kawach.app.R.string.sms_sos_template, mapsLink
            )
            scope.launch(Dispatchers.IO) {
                SmsHelper.sendBulkSms(contacts.map { it.phoneNumber }, message)
            }
        }

        // ── 3. Siren at max volume ────────────────────────────────────────────
        startSiren()

        // ── 4. Torch strobe (coroutine-cancellable) ───────────────────────────
        torchJob = scope.launch(Dispatchers.Default) {
            try {
                cameraId = cameraManager.cameraIdList.firstOrNull()
                // isActive here refers to the coroutine's isActive, not sosRunning —
                // loop stops automatically when torchJob is cancelled in deactivate().
                while (isActive) {
                    cameraId?.let { cameraManager.setTorchMode(it, true) }
                    delay(300)
                    cameraId?.let { cameraManager.setTorchMode(it, false) }
                    delay(300)
                }
            } catch (_: Exception) { /* camera/torch unavailable on this device */ }
        }

        // ── 5. Vibrate ────────────────────────────────────────────────────────
        vibrate()
    }

    /** Stop siren, torch strobe, and vibration. Safe to call from any thread. */
    fun deactivate() {
        sosRunning = false
        sirenPlayer?.stop()
        sirenPlayer?.release()
        sirenPlayer = null
        torchJob?.cancel()
        try { cameraId?.let { cameraManager.setTorchMode(it, false) } } catch (_: Exception) {}
    }

    private fun startSiren() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        sirenPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val pattern = longArrayOf(0, 500, 200, 500, 200)   // pause, on, off repeating
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }
}
