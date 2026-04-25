package com.example.focusflowplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}