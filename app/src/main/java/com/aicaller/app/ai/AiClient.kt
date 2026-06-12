package com.aicaller.app.ai

/**
 * Abstraction over the AI backend used for call summarization, spam
 * analysis, auto-reply drafting and voice command parsing.
 *
 * The production implementation ([AnthropicAiClient]) calls the Claude API.
 * When no API key is configured, [LocalHeuristicAiClient] provides
 * on-device fallbacks so the app remains fully functional offline.
 */
interface AiClient {

    suspend fun summarizeCall(transcript: String, contactName: String?): CallSummaryResult

    suspend fun analyzeSpamRisk(phoneNumber: String, recentTranscriptSnippet: String?): SpamAnalysisResult

    suspend fun generateAutoReply(context: AutoReplyContext): String

    suspend fun parseVoiceCommand(utterance: String, knownContactNames: List<String>): VoiceCommandResult
}
