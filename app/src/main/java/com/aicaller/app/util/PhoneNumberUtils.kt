package com.aicaller.app.util

/** Normalizes phone numbers so they can be reliably matched/keyed across providers. */
object PhoneNumberUtils {

    fun normalize(number: String): String {
        val trimmed = number.trim()
        val keepLeadingPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (keepLeadingPlus) "+$digits" else digits
    }
}
