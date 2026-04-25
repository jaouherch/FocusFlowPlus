package com.example.focusflowplus.domain.model

data class AiRecommendation(
    val focusMinutes: Int,
    val breakMinutes: Int,
    val sessionStructure: String,
    val healthTips: List<String>,
    val motivationalMessage: String,
    val rawResponse: String = ""
)