package com.aicaller.app.ai

import java.util.Calendar
import javax.inject.Inject

/**
 * Offline fallback used when no API key is configured, or when a network
 * call fails. Keeps every feature usable without an internet connection,
 * just with less nuanced results than the Gemini-backed client.
 */
class LocalHeuristicAiClient @Inject constructor() : AiClient {

    override suspend fun summarizeCall(transcript: String, contactName: String?): CallSummaryResult {
        val sentences = transcript.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val summary = if (sentences.isEmpty()) {
            "No speech detected during this call."
        } else {
            sentences.take(2).joinToString(" ")
        }
        val actionItems = sentences.filter { sentence ->
            Regex("\\b(call back|follow up|send|email|schedule|remind|need to|should)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(sentence)
        }.take(5)

        val positiveWords = Regex("\\b(thanks|great|good|appreciate|awesome|perfect)\\b", RegexOption.IGNORE_CASE)
        val negativeWords = Regex("\\b(angry|problem|issue|complaint|bad|frustrat\\w*|cancel)\\b", RegexOption.IGNORE_CASE)
        val positives = positiveWords.findAll(transcript).count()
        val negatives = negativeWords.findAll(transcript).count()
        val sentiment = when {
            positives + negatives == 0 -> 0f
            else -> ((positives - negatives).toFloat() / (positives + negatives)).coerceIn(-1f, 1f)
        }

        return CallSummaryResult(
            summary = summary,
            actionItems = actionItems,
            sentiment = sentiment
        )
    }

    override suspend fun analyzeSpamRisk(phoneNumber: String, recentTranscriptSnippet: String?): SpamAnalysisResult {
        return SpamHeuristics.score(phoneNumber)
    }

    override suspend fun generateAutoReply(context: AutoReplyContext): String {
        if (context.isLikelySpam) {
            return "I'm currently unavailable. Please remove this number from your calling list."
        }
        val name = context.callerName?.let { "Hi $it, " } ?: "Hi, "
        return "${name}sorry I missed your call ${context.timeOfDay.lowercase()} - I'll call you back as soon as I can."
    }

    override suspend fun parseVoiceCommand(utterance: String, knownContactNames: List<String>): VoiceCommandResult {
        val lower = utterance.lowercase().trim()
        return when {
            lower.startsWith("call ") || lower.startsWith("dial ") -> {
                val name = lower.substringAfter(" ").trim()
                val match = knownContactNames.firstOrNull { it.lowercase().contains(name) }
                VoiceCommandResult(VoiceAction.CALL_CONTACT, target = match ?: name)
            }
            lower.startsWith("block ") -> {
                VoiceCommandResult(VoiceAction.BLOCK_NUMBER, target = lower.removePrefix("block ").trim())
            }
            lower.contains("last call") || lower.contains("last summary") || lower.contains("read my last") -> {
                VoiceCommandResult(VoiceAction.READ_LAST_SUMMARY)
            }
            lower.startsWith("search ") || lower.startsWith("find ") -> {
                VoiceCommandResult(VoiceAction.SEARCH_CALL_HISTORY, target = lower.substringAfter(" ").trim())
            }
            else -> VoiceCommandResult(VoiceAction.UNKNOWN, message = "Sorry, I didn't understand that.")
        }
    }

    override suspend fun chat(message: String, history: List<ChatMessage>, context: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi ") || lower == "hi" ->
                "Hi there! I can help with calls, contacts, and your call history. What would you like to do?"
            context.isNotBlank() && (lower.contains("summary") || lower.contains("recent") || lower.contains("call")) ->
                "Here's what I know from recent activity:\n$context"
            lower.contains("thank") ->
                "You're welcome!"
            else ->
                "I'm running in offline mode right now, so I can't have a full conversation, but I can still place calls, block numbers, and search your call history."
        }
    }

    companion object {
        fun timeOfDayLabel(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour < 12 -> "this morning"
                hour < 17 -> "this afternoon"
                else -> "this evening"
            }
        }
    }
}
