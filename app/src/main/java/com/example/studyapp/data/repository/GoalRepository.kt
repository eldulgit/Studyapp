package com.example.studyapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.studyapp.data.GoalPreferences
import com.example.studyapp.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(private val context: Context) {

    suspend fun saveDailyGoal(minutes: Int) {
        context.dataStore.edit {
            it[GoalPreferences.DAILY_GOAL] = minutes
        }
    }

    suspend fun saveWeeklyGoal(minutes: Int) {
        context.dataStore.edit {
            it[GoalPreferences.WEEKLY_GOAL] = minutes
        }
    }

    suspend fun saveMonthlyGoal(minutes: Int) {
        context.dataStore.edit {
            it[GoalPreferences.MONTHLY_GOAL] = minutes
        }
    }

    fun getDailyGoal(): Flow<Int> =
        context.dataStore.data.map {
            it[GoalPreferences.DAILY_GOAL] ?: 240
        }

    fun getWeeklyGoal(): Flow<Int> =
        context.dataStore.data.map {
            it[GoalPreferences.WEEKLY_GOAL] ?: 1200
        }

    fun getMonthlyGoal(): Flow<Int> =
        context.dataStore.data.map {
            it[GoalPreferences.MONTHLY_GOAL] ?: 3000
        }
}
