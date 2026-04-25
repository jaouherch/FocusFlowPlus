package com.example.focusflowplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT COUNT(*) FROM sessions WHERE wasCompleted = 1")
    fun getCompletedSessionCount(): Flow<Int>

    @Query("SELECT SUM(focusMinutes) FROM sessions WHERE wasCompleted = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("""
        SELECT COUNT(*) FROM sessions 
        WHERE wasCompleted = 1 
        AND completedAt >= :startOfDay
    """)
    fun getSessionsCompletedToday(startOfDay: Long): Flow<Int>
}