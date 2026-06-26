package com.shingar.salon.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtils {

    fun sendMessage(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(" ", "").replace("+", "")
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun sendAppointmentConfirmation(context: Context, phone: String, customerName: String, service: String, dateTime: String) {
        val message = """
            Hello $customerName! 💇‍♀️

            Your appointment at Shingar Sallon has been confirmed!

            📋 Service: $service
            📅 Date & Time: $dateTime

            We look forward to seeing you!

            - Shingar Sallon ✨
        """.trimIndent()
        sendMessage(context, phone, message)
    }

    fun sendReminder(context: Context, phone: String, customerName: String, service: String, dateTime: String) {
        val message = """
            Hi $customerName! 🔔

            This is a friendly reminder about your upcoming appointment at Shingar Sallon.

            📋 Service: $service
            📅 Date & Time: $dateTime

            See you soon!

            - Shingar Sallon ✨
        """.trimIndent()
        sendMessage(context, phone, message)
    }

    fun shareOffer(context: Context, phone: String, offerTitle: String, offerDescription: String) {
        val message = """
            🌟 Special Offer from Shingar Sallon! 🌟

            $offerTitle

            $offerDescription

            Book now! Call us or visit our salon.

            - Shingar Sallon ✨
        """.trimIndent()
        sendMessage(context, phone, message)
    }
}
