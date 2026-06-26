package com.shingar.salon.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatDateTime(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
}

fun LocalDateTime.formatTime(): String {
    return this.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

fun LocalDateTime.formatDate(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun LocalDate.formatDate(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun Double.formatCurrency(): String {
    return "₹${String.format("%,.0f", this)}"
}

fun Double.formatCurrencyDecimal(): String {
    return "₹${String.format("%,.2f", this)}"
}
