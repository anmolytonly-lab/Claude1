package com.kawach.app.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects a hard 3-shake pattern within [windowMs] milliseconds.
 * Register via [SensorManager.registerListener]; unregister when done.
 */
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    companion object {
        private const val SHAKE_THRESHOLD_G = 2.7f   // gravitational units
        private const val MIN_SHAKES = 3
        private const val WINDOW_MS = 1500L
    }

    private var shakeTimestamps = mutableListOf<Long>()

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > SHAKE_THRESHOLD_G) {
            val now = System.currentTimeMillis()
            shakeTimestamps.add(now)
            // Keep only timestamps within the detection window
            shakeTimestamps = shakeTimestamps.filter { now - it < WINDOW_MS }.toMutableList()

            if (shakeTimestamps.size >= MIN_SHAKES) {
                shakeTimestamps.clear()
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
