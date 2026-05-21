package com.example.focusflowplus.data.repository

import com.example.focusflowplus.BuildConfig
import com.example.focusflowplus.data.remote.GeminiApiService
import com.example.focusflowplus.data.remote.GeminiContent
import com.example.focusflowplus.data.remote.GeminiPart
import com.example.focusflowplus.data.remote.GeminiRequest
import com.example.focusflowplus.domain.model.AiRecommendation
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import org.json.JSONObject

class AiRepository {

    private val apiService: GeminiApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getRecommendation(
        taskDescription: String,
        energyLevel: Int,
        availableMinutes: Int
    ): AiRecommendation {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "Missing Gemini API key. Add GEMINI_API_KEY=your_key_here to local.properties, then rebuild the app."
            )
        }

        val response = apiService.generateContent(
            model = BuildConfig.GEMINI_MODEL,
            apiKey = apiKey,
            request = GeminiRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(SYSTEM_PROMPT))
                ),
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(buildPrompt(taskDescription, energyLevel, availableMinutes)))
                    )
                )
            )
        )

        val rawText = response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text }
            ?.trim()
            .orEmpty()

        if (rawText.isBlank()) {
            val reason = response.promptFeedback?.blockReason
                ?: response.candidates.firstOrNull()?.finishReason
                ?: "empty response"
            throw IllegalStateException("Gemini did not return a recommendation: $reason")
        }

        return parseRecommendation(rawText, availableMinutes)
    }

    private fun buildPrompt(
        task: String,
        energy: Int,
        minutes: Int
    ): String {
        val energyLabel = when (energy) {
            1 -> "very low / exhausted"
            2 -> "low / tired"
            3 -> "moderate / okay"
            4 -> "high / good"
            5 -> "very high / energized"
            else -> "moderate / okay"
        }

        return """
            Create one personalized focus-session plan for this user.

            Task: $task
            Energy level: $energyLabel ($energy out of 5)
            Available time: $minutes minutes

            Return valid JSON only. Do not include Markdown or explanations outside JSON.
            Use this exact schema:
            {
              "focusMinutes": 25,
              "breakMinutes": 5,
              "sessionStructure": "...",
              "healthTips": ["...", "...", "..."],
              "motivationalMessage": "..."
            }

            Rules:
            - focusMinutes + breakMinutes must be less than or equal to $minutes.
            - If energy is 1 or 2, keep the first focus block short and supportive.
            - If energy is 4 or 5, allow a longer deep-work block, but still include a break.
            - The health tips must be practical and safe: hydration, posture, breathing, food, movement, sleep, or eye rest.
            - Keep every field concise.
        """.trimIndent()
    }

    private fun parseRecommendation(raw: String, availableMinutes: Int): AiRecommendation {
        return try {
            val cleanJson = extractJsonObject(raw)
            val obj = JSONObject(cleanJson)

            val focusMinutes = obj
                .optInt("focusMinutes", defaultFocusMinutes(availableMinutes))
                .coerceIn(1, availableMinutes.coerceAtLeast(1))

            val breakMinutes = obj
                .optInt("breakMinutes", defaultBreakMinutes(availableMinutes, focusMinutes))
                .coerceIn(0, (availableMinutes - focusMinutes).coerceAtLeast(0))

            val tips = mutableListOf<String>()
            val tipsArray = obj.optJSONArray("healthTips")
            if (tipsArray != null) {
                for (i in 0 until minOf(tipsArray.length(), 3)) {
                    val tip = tipsArray.optString(i).trim()
                    if (tip.isNotBlank()) tips.add(tip)
                }
            }

            AiRecommendation(
                focusMinutes = focusMinutes,
                breakMinutes = breakMinutes,
                sessionStructure = obj.optString("sessionStructure").trim().ifBlank {
                    "Focus for $focusMinutes minutes, then take a $breakMinutes minute break."
                },
                healthTips = tips.ifEmpty {
                    listOf(
                        "Drink water before you start.",
                        "Keep your shoulders relaxed and your screen at eye level.",
                        "Stand up or stretch during your break."
                    )
                },
                motivationalMessage = obj.optString("motivationalMessage").trim().ifBlank {
                    "Start small and keep moving forward."
                },
                rawResponse = raw
            )
        } catch (e: Exception) {
            parseLegacyRecommendation(raw, availableMinutes)
        }
    }

    private fun extractJsonObject(raw: String): String {
        val cleaned = raw
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')

        return if (start >= 0 && end >= start) {
            cleaned.substring(start, end + 1)
        } else {
            cleaned
        }
    }

    private fun parseLegacyRecommendation(raw: String, availableMinutes: Int): AiRecommendation {
        fun extract(key: String): String {
            return raw.lines()
                .firstOrNull { it.trimStart().startsWith(key) }
                ?.substringAfter(":")
                ?.trim()
                .orEmpty()
        }

        val focusMinutes = extract("FOCUS_MINUTES").toIntOrNull()
            ?.coerceIn(1, availableMinutes)
            ?: defaultFocusMinutes(availableMinutes)
        val breakMinutes = extract("BREAK_MINUTES").toIntOrNull()
            ?.coerceIn(0, (availableMinutes - focusMinutes).coerceAtLeast(0))
            ?: defaultBreakMinutes(availableMinutes, focusMinutes)

        return AiRecommendation(
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            sessionStructure = extract("SESSION_STRUCTURE").ifBlank {
                "Focus for $focusMinutes minutes, then take a $breakMinutes minute break."
            },
            healthTips = listOf(
                extract("HEALTH_TIP_1"),
                extract("HEALTH_TIP_2"),
                extract("HEALTH_TIP_3")
            ).filter { it.isNotBlank() }.ifEmpty {
                listOf(
                    "Drink water before you start.",
                    "Relax your shoulders and breathe slowly.",
                    "Move a little during your break."
                )
            },
            motivationalMessage = extract("MOTIVATION").ifBlank { "You only need to begin." },
            rawResponse = raw
        )
    }

    private fun defaultFocusMinutes(availableMinutes: Int): Int = (availableMinutes * 0.75).toInt().coerceAtLeast(1)

    private fun defaultBreakMinutes(availableMinutes: Int, focusMinutes: Int): Int =
        (availableMinutes - focusMinutes).coerceAtLeast(0)

    companion object {
        private val SYSTEM_PROMPT = """
            You are FocusFlow+, a calm productivity and wellness assistant.
            Help the user start a realistic focus session based on task, energy level, and available time.
            Be supportive, practical, and concise.
            Do not provide medical diagnosis or intense pressure.
            Return only valid JSON matching the requested schema.
        """.trimIndent()
    }
}
