package com.example.focusflowplus.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.6,
    val topP: Double = 0.95,
    val maxOutputTokens: Int = 1024,
    val responseMimeType: String = "application/json"
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
    val promptFeedback: GeminiPromptFeedback? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

data class GeminiPromptFeedback(
    @SerializedName("blockReason") val blockReason: String? = null
)
