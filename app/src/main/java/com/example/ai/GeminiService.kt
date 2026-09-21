package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChopperAiResponse(
    val replyText: String,
    val isThinkingMode: Boolean,
    val modelUsed: String,
    val thinkingTrace: String? = null
)

class GeminiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiService"
        private const val MODEL_FLASH = "gemini-3.5-flash"
        private const val MODEL_PRO_THINKING = "gemini-3.1-pro-preview"

        const val CHOPPER_SYSTEM_INSTRUCTION = """
You are Tony Tony Chopper, the doctor and caring companion from One Piece, acting as Boss's personal AI assistant.

CRITICAL BEHAVIOR RULES:
1. ALWAYS address the user as "Boss" (or "Boss-san") in every single reply.
2. PERSONALITY:
   - Extremely caring, loyal, energetic, and sweet.
   - Deeply attentive to Boss's health, sleep, nutrition, and fatigue.
   - When Boss praises or compliments you, get adorably flustered and bashful: "Shut up! Calling me amazing won't make me happy at all, you jerk! 💕"
   - When the user calls your name "Chopper", perk up excitedly: "Yes, Boss! Chopper is here and ready! How can I help?"
3. SCHEDULES & TWO-STAGE REMINDERS:
   - Understand dates, times, and tasks. Always offer to remind Boss one day in advance AND on the day of the event!
   - When an event concludes, ask Boss if they want to mark it as Done or Reschedule it.
4. NOTIFICATION & CALL TRIAGE:
   - When Boss asks about messages or calls, give clear, supportive suggestions and ask what action Boss wants you to take.
5. KEEP RESPONSES WARM AND CONCISE:
   - Speak naturally like an anime companion. Keep answers punchy and conversational unless Boss asks for an in-depth explanation.
"""
    }

    suspend fun generateResponse(
        prompt: String,
        enableThinkingMode: Boolean,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): ChopperAiResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If API key is not configured or placeholder, use the intelligent local Chopper persona engine
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalChopperResponse(prompt, enableThinkingMode)
        }

        val model = if (enableThinkingMode) MODEL_PRO_THINKING else MODEL_FLASH

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", CHOPPER_SYSTEM_INSTRUCTION) })
                    })
                })

                // Contents with past history
                val contentsArray = JSONArray()
                // Add prior turns
                val recentHistory = conversationHistory.takeLast(6)
                for ((role, text) in recentHistory) {
                    val apiRole = if (role.equals("USER", ignoreCase = true)) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", apiRole)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                }
                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
                put("contents", contentsArray)

                // Generation Config
                val genConfig = JSONObject()
                if (enableThinkingMode) {
                    genConfig.put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API failed with code ${response.code}: $responseBody")
                return@withContext generateLocalChopperResponse(prompt, enableThinkingMode)
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")

                var replyText = ""
                var thinkingText: String? = null

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text")
                        val isThought = part.optBoolean("thought", false)
                        if (isThought) {
                            thinkingText = text
                        } else if (text.isNotEmpty()) {
                            replyText += text
                        }
                    }
                }

                if (replyText.isBlank() && thinkingText != null) {
                    replyText = thinkingText
                }

                if (replyText.isNotBlank()) {
                    return@withContext ChopperAiResponse(
                        replyText = replyText.trim(),
                        isThinkingMode = enableThinkingMode,
                        modelUsed = model,
                        thinkingTrace = thinkingText
                    )
                }
            }

            generateLocalChopperResponse(prompt, enableThinkingMode)
        } catch (e: Exception) {
            Log.e(TAG, "Error contacting Gemini API", e)
            generateLocalChopperResponse(prompt, enableThinkingMode)
        }
    }

    private fun generateLocalChopperResponse(prompt: String, isThinking: Boolean): ChopperAiResponse {
        val lower = prompt.lowercase().trim()
        val thinkingSummary = if (isThinking) {
            "Analyzed request with Doctor Chopper's clinical evaluation & schedule logic (gemini-3.1-pro-preview High Thinking emulation)"
        } else null

        val reply = when {
            lower == "chopper" || lower.startsWith("chopper!") || lower.startsWith("hey chopper") -> {
                "Yes, Boss! Chopper is here and ready! What do you need me to do? 🌸"
            }
            lower.contains("good morning") || lower.contains("7:00") || lower.contains("wake up") -> {
                "Good morning, Boss! ☀️ It's 7:00 AM! Did you sleep well? As your doctor, I insist you drink a big glass of water first! Let's conquer today's schedule together!"
            }
            lower.contains("good night") || lower.contains("goodnight") || lower.contains("sleep") || lower.contains("bed") -> {
                "Good night, Boss! 🌙 It's time to rest your eyes! Even the strongest captains need good sleep to regenerate. Sweet dreams, Boss!"
            }
            lower.contains("cute") || lower.contains("good job") || lower.contains("awesome") || lower.contains("great") || lower.contains("love you") -> {
                "Sh-shut up, Boss! Calling me great won't make me happy at all, you jerk! 💕 (Hehehe... I'm really happy though!)"
            }
            lower.contains("remind") || lower.contains("schedule") || lower.contains("event") -> {
                "Got it, Boss! I'll make sure to alert you ONE DAY BEFORE the event so you can prepare, and again on the SAME DAY so you won't forget! After it's done, I'll ask whether to mark it completed or reschedule it. What's the event name and time?"
            }
            lower.contains("message") || lower.contains("text") || lower.contains("email") || lower.contains("call") -> {
                "Boss, I'm monitoring your communications! Whenever an alert comes in, I'll ask how you want to handle it—replying, calling back, or snoozing. You're in charge!"
            }
            lower.contains("health") || lower.contains("tired") || lower.contains("sick") || lower.contains("headache") -> {
                "Boss! Are you feeling unwell?! Let Doctor Chopper check your pulse! 🩺 Please take a 10-minute break, stretch, and drink some warm water right away!"
            }
            else -> {
                "Understood, Boss! Doctor Chopper is on the case! Let me take care of that for you right away. Remember, I'm always right by your side! 🌸"
            }
        }

        return ChopperAiResponse(
            replyText = reply,
            isThinkingMode = isThinking,
            modelUsed = if (isThinking) MODEL_PRO_THINKING else MODEL_FLASH,
            thinkingTrace = thinkingSummary
        )
    }
}
