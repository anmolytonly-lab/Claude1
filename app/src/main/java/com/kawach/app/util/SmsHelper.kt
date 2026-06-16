package com.kawach.app.util

import android.telephony.SmsManager
import android.os.Build

object SmsHelper {

    /**
     * Sends [message] to each number in [recipients].
     * Long messages are automatically split by SmsManager.
     * Returns a list of numbers where sending failed (empty = all sent OK).
     * Does NOT need internet — uses device SIM card.
     */
    fun sendBulkSms(recipients: List<String>, message: String): List<String> {
        val failed = mutableListOf<String>()
        val smsManager = getSmsManager()

        for (number in recipients) {
            try {
                val parts = smsManager.divideMessage(message)
                if (parts.size == 1) {
                    smsManager.sendTextMessage(number, null, message, null, null)
                } else {
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                }
            } catch (e: Exception) {
                failed.add(number)
            }
        }
        return failed
    }

    @Suppress("DEPRECATION")
    private fun getSmsManager(): SmsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+ requires context-based SmsManager via getSystemService
            // but SmsManager.getDefault() still works for sending
            SmsManager.getDefault()
        } else {
            SmsManager.getDefault()
        }
}
