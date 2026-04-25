package com.example.focusflowplus.data.repository

import com.example.focusflowplus.data.local.SessionDao
import com.example.focusflowplus.domain.model.FocusSession
import com.example.focusflowplus.domain.model.toDomain
import com.example.focusflowplus.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(private val dao: SessionDao) {

    suspend fun saveSession(session: FocusSession): Long {
        return dao.insertSession(session.toEntity())
    }

    suspend fun updateSession(session: FocusSession) {
        dao.updateSession(session.toEntity())
    }

    fun getAllSessions(): Flow<List<FocusSession>> {
        return dao.getAllSessions().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getSessionById(id: Long): FocusSession? {
        return dao.getSessionById(id)?.toDomain()
    }

    fun getCompletedSessionCount(): Flow<Int> = dao.getCompletedSessionCount()

    fun getTotalFocusMinutes(): Flow<Int?> = dao.getTotalFocusMinutes()

    fun getSessionsCompletedToday(startOfDay: Long): Flow<Int> =
        dao.getSessionsCompletedToday(startOfDay)
}