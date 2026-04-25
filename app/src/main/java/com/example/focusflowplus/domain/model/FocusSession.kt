package com.example.focusflowplus.domain.model

data class FocusSession(
    val id: Long = 0,
    val taskDescription: String,
    val energyLevel: Int,
    val availableMinutes: Int,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val wasCompleted: Boolean = false,
    val aiRecommendation: String = ""
)

fun FocusSession.toEntity() = com.example.focusflowplus.data.local.SessionEntity(
    id = id,
    taskDescription = taskDescription,
    energyLevel = energyLevel,
    availableMinutes = availableMinutes,
    focusMinutes = focusMinutes,
    breakMinutes = breakMinutes,
    completedAt = completedAt,
    wasCompleted = wasCompleted,
    aiRecommendation = aiRecommendation
)

fun com.example.focusflowplus.data.local.SessionEntity.toDomain() = FocusSession(
    id = id,
    taskDescription = taskDescription,
    energyLevel = energyLevel,
    availableMinutes = availableMinutes,
    focusMinutes = focusMinutes,
    breakMinutes = breakMinutes,
    completedAt = completedAt,
    wasCompleted = wasCompleted,
    aiRecommendation = aiRecommendation
)