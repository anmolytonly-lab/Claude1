package com.aicaller.app.data.repository

/** Lightweight contact projection read from the device contacts provider. */
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)
