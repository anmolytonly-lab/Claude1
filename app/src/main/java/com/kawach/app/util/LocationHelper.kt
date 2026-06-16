package com.kawach.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object LocationHelper {

    /** Returns a Google Maps link for the given coordinates. */
    fun toMapsLink(lat: Double, lon: Double): String =
        "https://maps.google.com/?q=$lat,$lon"

    /**
     * Fetches the current location once (on-demand — no background polling).
     * Throws [SecurityException] if the location permission is missing.
     * Returns null if location is unavailable on the device.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            cont.invokeOnCancellation { cts.cancel() }

            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location -> cont.resume(location) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
}
