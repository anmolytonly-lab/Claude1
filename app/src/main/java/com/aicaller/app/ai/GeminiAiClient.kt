package com.aicaller.app.ai

import com.aicaller.app.util.SecurePrefs
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini-backed [AiClient]. Falls back to [LocalHeuristicAiClient] when no
 * API key is configured or the request fails, so every feature keeps
 * working (with reduced fidelity) offline.
 */
@Singleton
class GeminiAiClient @Inject constructor(
    private val api: GeminiApi,
    private val securePrefs: SecurePrefs,
    private val fallback: LocalHeuristicAiClient,
    private val gson: Gson
) : AiClient {

    private suspend fun <T> withGemini(block: suspend (apiKey: String) -> T, onFallback: suspend () -> T): T {
        val apiKey = securePrefs.geminiApiKey
        if (apiKey.isNullOrBlank()) return onFallback()
        return try {
            withContext(Dispatchers.IO) { block(apiKey) }
        } catch (e: Exception) {
            onFallback()
        }
    }

    private suspend fun generate(apiKey: String, prompt: String, maxOutputTokens: Int = 1024): String {
        val response = api.generateContent(
            model = GeminiApi.MODEL,
            apiKey = apiKey,
            request = GeminiGenerateRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(temperature = 0.4, maxOutputTokens = maxOutputTokens)
            )
        )
        return response.text
    }

    override suspend fun summarizeCall(transcript: String, contactName: String?): CallSummaryResult =
        withGemini(
            block = { apiKey ->
                val prompt = """
                    Summarize the following phone call transcript${contactName?.let { " with $it" } ?: ""}.
                    Respond with ONLY raw JSON (no markdown fences) matching this schema:
                    {"summary": string, "actionItems": string[], "sentiment": number between -1 and 1, "suggestedTags": string[], "relationship": string or null}

                    Transcript:
                    $transcript
                """.trimIndent()

                parseJson<CallSummaryResult>(generate(apiKey, prompt)) ?: fallback.summarizeCall(transcript, contactName)
            },
            onFallback = { fallback.summarizeCall(transcript, contactName) }
        )

    override suspend fun analyzeSpamRisk(phoneNumber: String, recentTranscriptSnippet: String?): SpamAnalysisResult =
        withGemini(
            block = { apiKey ->
                val prompt = """
                    Assess the spam/scam risk of an incoming phone call from "$phoneNumber".
                    ${recentTranscriptSnippet?.let { "A snippet from a recent call with this number: \"$it\"." } ?: ""}
                    Respond with ONLY raw JSON (no markdown fences) matching this schema:
                    {"riskScore": integer 0-100, "label": string, "reason": string, "shouldBlock": boolean, "shouldScreen": boolean}
                """.trimIndent()

                parseJson<SpamAnalysisResult>(generate(apiKey, prompt, maxOutputTokens = 400))
                    ?: fallback.analyzeSpamRisk(phoneNumber, recentTranscriptSnippet)
            },
            onFallback = { fallback.analyzeSpamRisk(phoneNumber, recentTranscriptSnippet) }
        )

    override suspend fun generateAutoReply(context: AutoReplyContext): String =
        withGemini(
            block = { apiKey ->
                val prompt = """
                    Draft a short, friendly SMS auto-reply (max 200 characters) to send to someone whose call was just
                    missed. Caller number: ${context.callerNumber}. Caller name: ${context.callerName ?: "unknown"}.
                    Relationship: ${context.relationship ?: "unknown"}. Time of day: ${context.timeOfDay}.
                    Likely spam: ${context.isLikelySpam}. Recent call summary: ${context.recentSummary ?: "none"}.
                    If likely spam, write a brief, neutral message that does not confirm the line is active for personal use.
                    Respond with ONLY the message text, no quotes, no JSON.
                """.trimIndent()

                generate(apiKey, prompt, maxOutputTokens = 150).trim().ifBlank { fallback.generateAutoReply(context) }
            },
            onFallback = { fallback.generateAutoReply(context) }
        )

    override suspend fun parseVoiceCommand(utterance: String, knownContactNames: List<String>): VoiceCommandResult =
        withGemini(
            block = { apiKey ->
                val prompt = """
                    Interpret this voice command for a phone dialer app: "$utterance"
                    Known contacts: ${knownContactNames.joinToString(", ")}
                    Respond with ONLY raw JSON (no markdown fences) matching this schema:
                    {"action": one of ["CALL_CONTACT","BLOCK_NUMBER","READ_LAST_SUMMARY","SEARCH_CALL_HISTORY","UNKNOWN"], "target": string or null, "message": string or null}
                """.trimIndent()

                parseJson<VoiceCommandResult>(generate(apiKey, prompt, maxOutputTokens = 200))
                    ?: fallback.parseVoiceCommand(utterance, knownContactNames)
            },
            onFallback = { fallback.parseVoiceCommand(utterance, knownContactNames) }
        )

    private inline fun <reified T> parseJson(rawText: String): T? {
        val cleaned = rawText.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        return try {
            gson.fromJson(cleaned, T::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
