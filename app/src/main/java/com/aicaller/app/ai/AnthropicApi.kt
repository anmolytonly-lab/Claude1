package com.aicaller.app.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/** Retrofit definition for the Claude Messages API. */
interface AnthropicApi {

    @Headers("content-type: application/json", "anthropic-version: 2023-06-01")
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: AnthropicMessageRequest
    ): AnthropicMessageResponse

    companion object {
        const val BASE_URL = "https://api.anthropic.com/"
        const val MODEL = "claude-sonnet-4-5"
    }
}

data class AnthropicMessageRequest(
    val model: String = AnthropicApi.MODEL,
    val max_tokens: Int = 1024,
    val system: String? = null,
    val messages: List<AnthropicMessage>
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicMessageResponse(
    val id: String? = null,
    val content: List<AnthropicContentBlock>? = null,
    val stop_reason: String? = null
) {
    val text: String
        get() = content?.joinToString("\n") { it.text.orEmpty() }.orEmpty()
}

data class AnthropicContentBlock(
    val type: String? = null,
    val text: String? = null
)
