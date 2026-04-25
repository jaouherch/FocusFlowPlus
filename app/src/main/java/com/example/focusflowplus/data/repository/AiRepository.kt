package com.example.focusflowplus.data.repository

import com.example.focusflowplus.BuildConfig
import com.example.focusflowplus.data.remote.ClaudeApiService
import com.example.focusflowplus.data.remote.ClaudeMessage
import com.example.focusflowplus.data.remote.ClaudeRequest
import com.example.focusflowplus.domain.model.AiRecommendation
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AiRepository {

    private val apiService: ClaudeApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeApiService::class.java)
    }
    suspend fun getRecommendation(
        taskDescription: String,
        energyLevel: Int,
        availableMinutes: Int
    ): AiRecommendation {
        val prompt = buildPrompt(taskDescription, energyLevel, availableMinutes)

        val response = apiService.sendMessage(
            ClaudeRequest(
                system = SYSTEM_PROMPT,
                messages = listOf(
                    ClaudeMessage(role = "user", content = prompt)
                )
            )
        )

        val rawText = response.content.firstOrNull()?.text ?: ""
        return parseRecommendation(rawText, availableMinutes)
    }

    private fun buildPrompt(
        task: String,
        energy: Int,
        minutes: Int
    ): String {
        val energyLabel = when (energy) {
            1 -> "very low (exhausted)"
            2 -> "low (tired)"
            3 -> "moderate (okay)"
            4 -> "high (good)"
            5 -> "very high (energized)"
            else -> "moderate"
        }

        return """
            I need help planning a focused work session with these details:
            - Task: $task
            - Energy level: $energyLabel ($energy/5)
            - Available time: $minutes minutes
            
            Please provide a structured response in exactly this format:
            
            FOCUS_MINUTES: [number]
            BREAK_MINUTES: [number]
            SESSION_STRUCTURE: [brief description of how to split the session]
            HEALTH_TIP_1: [health or wellness tip based on energy level]
            HEALTH_TIP_2: [another practical tip - hydration, posture, food, movement]
            HEALTH_TIP_3: [one more tip relevant to their situation]
            MOTIVATION: [one short encouraging sentence]
        """.trimIndent()
    }

    private fun parseRecommendation(raw: String, availableMinutes: Int): AiRecommendation {
        fun extract(key: String): String {
            return raw.lines()
                .firstOrNull { it.startsWith(key) }
                ?.substringAfter(":")
                ?.trim() ?: ""
        }

        val focusMinutes = extract("FOCUS_MINUTES").toIntOrNull()
            ?: (availableMinutes * 0.75).toInt()
        val breakMinutes = extract("BREAK_MINUTES").toIntOrNull()
            ?: (availableMinutes * 0.25).toInt()

        val healthTips = listOf(
            extract("HEALTH_TIP_1"),
            extract("HEALTH_TIP_2"),
            extract("HEALTH_TIP_3")
        ).filter { it.isNotEmpty() }

        return AiRecommendation(
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            sessionStructure = extract("SESSION_STRUCTURE"),
            healthTips = healthTips,
            motivationalMessage = extract("MOTIVATION"),
            rawResponse = raw
        )
    }

    companion object {
        private val SYSTEM_PROMPT = """
            You are FocusFlow+, a compassionate productivity and wellness assistant. 
            Your role is to help users work smarter by creating personalized focus sessions.
            
            Always consider:
            - The user's energy level when recommending session length
            - Practical health advice (hydration, nutrition, movement, rest)
            - Realistic and encouraging tone
            - If energy is low (1-2), recommend shorter sessions and more breaks
            - If energy is high (4-5), recommend longer focus blocks
            - Always include at least one hydration or nutrition reminder
            
            Be warm, practical, and supportive. Keep tips concise and actionable.
        """.trimIndent()
    }
}