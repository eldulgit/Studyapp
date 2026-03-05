package com.example.studyapp.data

import androidx.datastore.preferences.core.intPreferencesKey

object GoalPreferences {
    val DAILY_GOAL = intPreferencesKey("daily_goal_minutes")
    val WEEKLY_GOAL = intPreferencesKey("weekly_goal_minutes")
    val MONTHLY_GOAL = intPreferencesKey("monthly_goal_minutes")
}
