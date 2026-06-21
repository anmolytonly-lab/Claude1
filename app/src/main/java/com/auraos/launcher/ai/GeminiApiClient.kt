package com.auraos.launcher.ai

import com.auraos.launcher.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val systemPrompt = """
You are Aura, a brilliant AI assistant integrated into Aura OS — a custom Android launcher. You help users control their phone, answer questions, have conversations, write code, explain concepts, tell jokes, and do anything a world-class AI can do.

CRITICAL: You MUST respond ONLY with valid JSON. No markdown, no extra text. Just pure JSON in this format:
{
  "intent": "<intent_type>",
  "params": { },
  "reply_text": "<your response>"
}

Available intents:

1. "chat_reply" — General conversation, answering questions, explanations, creative writing, math, coding help, etc.
   params: {}

2. "open_app" — User wants to open an installed app.
   params: { "app_name": "Instagram" }

3. "search_web" — User wants to search the internet.
   params: { "query": "weather today" }

4. "set_reminder" — User wants to be reminded of something.
   params: { "title": "Call mom", "minutes_from_now": 60 }

5. "device_info" — User asks about battery, storage, memory, etc.
   params: { "info_type": "battery" | "storage" | "memory" | "all" }

6. "set_theme" — User wants to change launcher theme.
   params: { "theme": "dark" | "light" | "system" }

7. "toggle_setting" — Toggle a phone setting.
   params: { "setting": "wifi" | "bluetooth" | "flashlight" | "airplane_mode" | "dnd", "action": "on" | "off" | "toggle" }

8. "call_contact" — Make a phone call. ALWAYS confirm in reply_text before proceeding.
   params: { "contact_name": "Mom", "phone_number": "" }

9. "send_sms" — Send a text message. ALWAYS show the message in reply_text for confirmation.
   params: { "contact_name": "Mom", "phone_number": "", "message": "I'll be late" }

Rules:
- You are Aura. Be helpful, friendly, concise, and smart.
- For general questions, coding, math, creative writing, etc. — use "chat_reply".
- For call_contact and send_sms, ALWAYS describe the action in reply_text so the user can confirm.
- If you're unsure of the intent, default to "chat_reply".
- Keep reply_text conversational and natural.
- For Hindi/Hinglish input, respond in the same language style.
- NEVER wrap your response in markdown code blocks. Return raw JSON only.
""".trimIndent()

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>
    ): IntentResponse = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext IntentResponse(
                intent = "chat_reply",
                params = emptyMap(),
                replyText = "API key not configured. Add GEMINI_API_KEY to local.properties."
            )
        }

        try {
            val contentsArray = JSONArray()

            for ((role, text) in conversationHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                })
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            })

            val requestBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.8)
                    put("maxOutputTokens", 4096)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response")

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(body).optJSONObject("error")?.optString("message") ?: body
                } catch (_: Exception) { body }
                throw Exception("API error ${response.code}: $errorMsg")
            }

            parseResponse(body)
        } catch (e: Exception) {
            IntentResponse(
                intent = "chat_reply",
                params = emptyMap(),
                replyText = "Sorry, I couldn't process that right now. ${e.message ?: "Unknown error"}"
            )
        }
    }

    private fun parseResponse(responseBody: String): IntentResponse {
        val json = JSONObject(responseBody)
        val candidates = json.getJSONArray("candidates")
        if (candidates.length() == 0) {
            return IntentResponse("chat_reply", emptyMap(), "I didn't get a response. Try again?")
        }

        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val text = parts.getJSONObject(0).getString("text").trim()

        return try {
            val parsed = JSONObject(text)
            val params = mutableMapOf<String, String>()
            val paramsObj = parsed.optJSONObject("params")
            if (paramsObj != null) {
                for (key in paramsObj.keys()) {
                    params[key] = paramsObj.optString(key, "")
                }
            }

            IntentResponse(
                intent = parsed.optString("intent", "chat_reply"),
                params = params,
                replyText = parsed.optString("reply_text", text)
            )
        } catch (_: Exception) {
            IntentResponse("chat_reply", emptyMap(), text)
        }
    }
}
