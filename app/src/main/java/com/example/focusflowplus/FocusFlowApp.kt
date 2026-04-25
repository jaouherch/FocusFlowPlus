package com.example.focusflowplus

import android.app.Application
import androidx.room.Room
import com.example.focusflowplus.data.local.FocusFlowDatabase
import com.example.focusflowplus.data.repository.AiRepository
import com.example.focusflowplus.data.repository.SessionRepository

class FocusFlowApp : Application() {

    val database by lazy {
        Room.databaseBuilder(
            this,
            FocusFlowDatabase::class.java,
            "focusflow_db"
        ).build()
    }

    val sessionRepository by lazy {
        SessionRepository(database.sessionDao())
    }

    val aiRepository by lazy {
        AiRepository()
    }
}