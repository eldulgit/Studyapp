package com.example.studyapp.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.studyapp.data.dataStore
import kotlinx.coroutines.flow.map

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    // 테마
    suspend fun saveTheme(theme: String) {
        dataStore.edit { it[stringPreferencesKey("theme")] = theme }
    }
    val themeFlow = dataStore.data.map {
        it[stringPreferencesKey("theme")] ?: "light"
    }

    // 알림 on/off
    suspend fun saveNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[booleanPreferencesKey("notification_enabled")] = enabled }
    }
    val notificationEnabledFlow = dataStore.data.map {
        it[booleanPreferencesKey("notification_enabled")] ?: true
    }

    // 목표 미달 알림
    suspend fun saveGoalAlertEnabled(enabled: Boolean) {
        dataStore.edit { it[booleanPreferencesKey("goal_alert_enabled")] = enabled }
    }
    val goalAlertEnabledFlow = dataStore.data.map {
        it[booleanPreferencesKey("goal_alert_enabled")] ?: false
    }

    // 알림 시간
    suspend fun saveNotificationTime(hour: String, minute: String) {
        dataStore.edit {
            it[stringPreferencesKey("notification_hour")] = hour
            it[stringPreferencesKey("notification_minute")] = minute
        }
    }
    val notificationHourFlow = dataStore.data.map {
        it[stringPreferencesKey("notification_hour")] ?: "08"
    }
    val notificationMinuteFlow = dataStore.data.map {
        it[stringPreferencesKey("notification_minute")] ?: "00"
    }

    // 코멘트 설정
    suspend fun saveCommentOption(option: String) {
        dataStore.edit { it[stringPreferencesKey("comment_option")] = option }
    }
    val commentOptionFlow = dataStore.data.map {
        it[stringPreferencesKey("comment_option")] ?: "오늘의 명언"
    }
}