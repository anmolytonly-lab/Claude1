package com.aicaller.app.ai

import java.util.Locale

/**
 * Lightweight on-device heuristics for spam scoring. Used as a fast first
 * pass before (or instead of) an AI lookup, and as the offline fallback.
 */
object SpamHeuristics {

    private val SUSPICIOUS_PREFIXES = listOf("+1900", "+1888", "0900", "+44844", "+44871")

    fun score(phoneNumber: String): SpamAnalysisResult {
        val normalized = phoneNumber.replace(Regex("[^+0-9]"), "")
        var score = 0
        val reasons = mutableListOf<String>()

        if (SUSPICIOUS_PREFIXES.any { normalized.startsWith(it) }) {
            score += 40
            reasons += "premium-rate prefix"
        }

        if (normalized.length <= 6) {
            score += 15
            reasons += "unusually short number"
        }

        // Repeated-digit patterns are common in robocall spoofing.
        val digitsOnly = normalized.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty() && digitsOnly.toSet().size <= 2) {
            score += 25
            reasons += "repeated digit pattern"
        }

        if (digitsOnly.windowed(4).any { window -> window.toSet().size == 1 }) {
            score += 10
            reasons += "sequential repeated block"
        }

        score = score.coerceIn(0, 100)
        val label = when {
            score >= 70 -> "Likely spam"
            score >= 35 -> "Suspicious"
            else -> "Unrated"
        }

        return SpamAnalysisResult(
            riskScore = score,
            label = label,
            reason = if (reasons.isEmpty()) "No risk signals detected" else reasons.joinToString(", ")
                .let { it.replaceFirstChar { c -> c.titlecase(Locale.ROOT) } },
            shouldBlock = score >= 80,
            shouldScreen = score >= 35
        )
    }
}
