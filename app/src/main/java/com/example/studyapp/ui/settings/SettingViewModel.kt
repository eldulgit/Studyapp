package com.example.studyapp.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.notification.StudyNotificationScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()


    var drowsinessAlertEnabled by mutableStateOf(true)
        private set
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
            launch {
                repo.drowsinessAlertEnabledFlow.collect {
                    drowsinessAlertEnabled = it
                }
            }
            launch { repo.themeFlow.collect { selectedTheme = it } }
            launch {
                combine(
                    repo.notificationEnabledFlow,
                    repo.notificationHourFlow,
                    repo.notificationMinuteFlow
                ) { enabled, hour, minute ->
                    Triple(enabled, hour, minute)
                }.collect { (enabled, hour, minute) ->
                    notificationEnabled = enabled
                    notificationHour = hour
                    notificationMinute = minute
                    updateScheduledStudyReminder()
                }
            }
            launch { repo.goalAlertEnabledFlow.collect { goalAlertEnabled = it } }
            launch { repo.commentOptionFlow.collect { commentOption = it } }
        }
    }

    fun updateTheme(theme: String) {
        selectedTheme = theme
        viewModelScope.launch { repo.saveTheme(theme) }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        notificationEnabled = enabled
        updateScheduledStudyReminder()
        viewModelScope.launch {
            repo.saveNotificationEnabled(enabled)
            saveNotificationSettingsToDb()
        }
    }

    fun updateDrowsinessAlertEnabled(enabled: Boolean) {
        drowsinessAlertEnabled = enabled

        viewModelScope.launch {
            repo.saveDrowsinessAlertEnabled(enabled)
        }
    }

    fun updateGoalAlertEnabled(enabled: Boolean) {
        goalAlertEnabled = enabled
        viewModelScope.launch { repo.saveGoalAlertEnabled(enabled) }
    }

    fun updateNotificationTime(hour: String, minute: String) {
        notificationHour = hour
        notificationMinute = minute
        updateScheduledStudyReminder()
        viewModelScope.launch {
            repo.saveNotificationTime(hour, minute)
            saveNotificationSettingsToDb()
        }
    }

    fun updateCommentOption(option: String) {
        commentOption = option
        viewModelScope.launch { repo.saveCommentOption(option) }
    }

    fun refreshStudyReminderSchedule() {
        updateScheduledStudyReminder()
    }

    private fun updateScheduledStudyReminder() {
        if (notificationEnabled) {
            StudyNotificationScheduler.scheduleDailyStudyReminder(
                context = getApplication(),
                hour = notificationHour,
                minute = notificationMinute
            )
        } else {
            StudyNotificationScheduler.cancelDailyStudyReminder(getApplication())
        }
    }

    fun loadNotificationSettingsFromDb() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getCurrentUid() ?: return@launch
                userRepository.ensureUserDocument(
                    uid = uid,
                    isGuest = authRepository.isCurrentUserAnonymous()
                )

                val profile = userRepository.getUserProfile(uid) ?: return@launch

                notificationEnabled = profile.notificationEnabled
                notificationHour = profile.notificationHour.ifBlank { "08" }
                notificationMinute = profile.notificationMinute.ifBlank { "00" }

                repo.saveNotificationEnabled(notificationEnabled)
                repo.saveNotificationTime(notificationHour, notificationMinute)
                updateScheduledStudyReminder()
            } catch (e: Exception) {
                android.util.Log.e("SettingsFirestore", "알림 설정 불러오기 실패", e)
            }
        }
    }

    private suspend fun saveNotificationSettingsToDb() {
        try {
            val uid = authRepository.getCurrentUid() ?: return
            userRepository.ensureUserDocument(
                uid = uid,
                isGuest = authRepository.isCurrentUserAnonymous()
            )

            userRepository.saveNotificationSettings(
                uid = uid,
                enabled = notificationEnabled,
                hour = notificationHour,
                minute = notificationMinute
            )
        } catch (e: Exception) {
            android.util.Log.e("SettingsFirestore", "알림 설정 저장 실패", e)
        }
    }
}
