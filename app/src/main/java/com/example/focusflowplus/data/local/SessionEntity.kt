package com.example.focusflowplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
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