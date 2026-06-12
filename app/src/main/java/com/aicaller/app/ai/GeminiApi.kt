package com.aicaller.app.ai

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit definition for the Google Gemini "generateContent" API. */
interface GeminiApi {

    @Headers("content-type: application/json")
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val MODEL = GeminiModelOptions.DEFAULT
    }
}

/** User-selectable Gemini models, traded off between speed/cost and quality. */
object GeminiModelOptions {
    const val FLASH_LITE = "gemini-2.0-flash-lite"
    const val FLASH = "gemini-2.0-flash"
    const val PRO = "gemini-1.5-pro"

    const val DEFAULT = FLASH

    val ALL = listOf(FLASH_LITE, FLASH, PRO)

    fun displayName(model: String): String = when (model) {
        FLASH_LITE -> "Gemini 2.0 Flash-Lite (fastest)"
        FLASH -> "Gemini 2.0 Flash (recommended)"
        PRO -> "Gemini 1.5 Pro (highest quality)"
        else -> model
    }
}

data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(val text: String? = null)

data class GeminiGenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null
)

data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null
) {
    val text: String
        get() = candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("") { it.text.orEmpty() }
            .orEmpty()
}

data class GeminiCandidate(
    val content: GeminiContent? = null
)
