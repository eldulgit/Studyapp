package com.example.studyapp.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    var selectedTheme by mutableStateOf("light")
        private set

    var notificationEnabled by mutableStateOf(true)
        private set

    var goalAlertEnabled by mutableStateOf(false)
        private set

    var notificationHour by mutableStateOf("08")
        private set

    var notificationMinute by mutableStateOf("00")
        private set

    var commentOption by mutableStateOf("오늘의 명언")
        private set

    init {
        viewModelScope.launch {
            launch { repo.themeFlow.collect { selectedTheme = it } }
            launch { repo.notificationEnabledFlow.collect { notificationEnabled = it } }
            launch { repo.goalAlertEnabledFlow.collect { goalAlertEnabled = it } }
            launch { repo.notificationHourFlow.collect { notificationHour = it } }
            launch { repo.notificationMinuteFlow.collect { notificationMinute = it } }
            launch { repo.commentOptionFlow.collect { commentOption = it } }
        }
    }

    fun updateTheme(theme: String) {
        selectedTheme = theme
        viewModelScope.launch { repo.saveTheme(theme) }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        notificationEnabled = enabled
        viewModelScope.launch { repo.saveNotificationEnabled(enabled) }
    }

    fun updateGoalAlertEnabled(enabled: Boolean) {
        goalAlertEnabled = enabled
        viewModelScope.launch { repo.saveGoalAlertEnabled(enabled) }
    }

    fun updateNotificationTime(hour: String, minute: String) {
        notificationHour = hour
        notificationMinute = minute
        viewModelScope.launch { repo.saveNotificationTime(hour, minute) }
    }

    fun updateCommentOption(option: String) {
        commentOption = option
        viewModelScope.launch { repo.saveCommentOption(option) }
    }
}