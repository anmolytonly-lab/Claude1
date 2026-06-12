package com.aicaller.app.ai

/**
 * Abstraction over the AI backend used for call summarization, spam
 * analysis, auto-reply drafting and voice command parsing.
 *
 * The production implementation ([GeminiAiClient]) calls the Google Gemini
 * API. When no API key is configured, [LocalHeuristicAiClient] provides
 * on-device fallbacks so the app remains fully functional offline.
 */
interface AiClient {

    suspend fun summarizeCall(transcript: String, contactName: String?): CallSummaryResult

    suspend fun analyzeSpamRisk(phoneNumber: String, recentTranscriptSnippet: String?): SpamAnalysisResult

    suspend fun generateAutoReply(context: AutoReplyContext): String

    suspend fun parseVoiceCommand(utterance: String, knownContactNames: List<String>): VoiceCommandResult

    /**
     * Free-form multi-turn chat used by the assistant for questions that
     * don't map to a known [VoiceAction]. [context] supplies grounding
     * information (e.g. recent call history) and [history] is the prior
     * conversation turns.
     */
    suspend fun chat(message: String, history: List<ChatMessage>, context: String): String
}
