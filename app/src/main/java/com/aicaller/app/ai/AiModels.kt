package com.aicaller.app.ai

/** A single turn in an assistant conversation, used for multi-turn context. */
data class ChatMessage(
    val role: ChatRole,
    val text: String
)

enum class ChatRole { USER, ASSISTANT }

/** Result of summarizing a call transcript. */
data class CallSummaryResult(
    val summary: String,
    val actionItems: List<String>,
    val sentiment: Float,          // -1.0 .. 1.0
    val suggestedTags: List<String> = emptyList(),
    val relationship: String? = null
)

/** Result of AI spam/risk analysis for an incoming number. */
data class SpamAnalysisResult(
    val riskScore: Int,            // 0..100
    val label: String,             // e.g. "Likely spam", "Safe", "Telemarketer"
    val reason: String,
    val shouldBlock: Boolean,
    val shouldScreen: Boolean
)

/** Context passed to the AI to draft a missed-call auto-reply. */
data class AutoReplyContext(
    val callerNumber: String,
    val callerName: String?,
    val relationship: String?,
    val timeOfDay: String,
    val isLikelySpam: Boolean,
    val recentSummary: String?
)

/** Result of interpreting a natural-language voice command. */
data class VoiceCommandResult(
    val action: VoiceAction,
    val target: String? = null,
    val message: String? = null
)

enum class VoiceAction {
    CALL_CONTACT,
    BLOCK_NUMBER,
    READ_LAST_SUMMARY,
    SEARCH_CALL_HISTORY,
    UNKNOWN
}
