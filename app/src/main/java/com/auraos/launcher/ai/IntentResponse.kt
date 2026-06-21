package com.auraos.launcher.ai

data class IntentResponse(
    val intent: String,
    val params: Map<String, String>,
    val replyText: String
)
