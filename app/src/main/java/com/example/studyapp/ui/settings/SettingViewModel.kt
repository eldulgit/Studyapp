package com.example.studyapp.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GoalRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.notification.GoalNotificationScheduler
import com.example.studyapp.notification.StudyNotificationScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val goalRepository = GoalRepository()

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

    var notificationSettingsLoaded by mutableStateOf(false)
        private set

    private var loadingNotificationSettings = false

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
                    if (!notificationSettingsLoaded && !loadingNotificationSettings) {
                        notificationEnabled = enabled
                        notificationHour = hour
                        notificationMinute = minute
                        updateScheduledStudyReminder()
                    }
                }
            }

            launch {
                repo.goalAlertEnabledFlow.collect {
                    goalAlertEnabled = it
                    refreshGoalReminderSchedule()
                }
            }
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
        viewModelScope.launch {
            repo.saveGoalAlertEnabled(enabled)
            refreshGoalReminderSchedule()
        }
    }

    fun updateNotificationTime(hour: String, minute: String) {
        notificationHour = hour
        notificationMinute = minute
        updateScheduledStudyReminder()
        refreshGoalReminderSchedule()
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

    fun refreshGoalReminderSchedule() {
        viewModelScope.launch {
            updateScheduledGoalReminders()
        }
    }

    fun loadNotificationSettingsFromDb() {
        viewModelScope.launch {
            try {
                loadingNotificationSettings = true
                notificationSettingsLoaded = false

                val uid = authRepository.getCurrentUid() ?: return@launch
                val settings = userRepository.getNotificationSettings(uid)

                if (settings != null) {
                    notificationEnabled = settings.enabled
                    notificationHour = settings.hour
                    notificationMinute = settings.minute
                    android.util.Log.d(
                        "SettingsFirestore",
                        "알림 설정 DB 불러오기 성공 uid=$uid enabled=$notificationEnabled time=$notificationHour:$notificationMinute"
                    )
                } else {
                    notificationEnabled = true
                    notificationHour = "08"
                    notificationMinute = "00"
                    android.util.Log.d(
                        "SettingsFirestore",
                        "DB 알림 설정 없음 uid=$uid, 기본값 표시 time=$notificationHour:$notificationMinute"
                    )
                }

                repo.saveNotificationEnabled(notificationEnabled)
                repo.saveNotificationTime(notificationHour, notificationMinute)
                updateScheduledStudyReminder()
                refreshGoalReminderSchedule()
            } catch (e: Exception) {
                android.util.Log.e("SettingsFirestore", "알림 설정 불러오기 실패", e)
            } finally {
                loadingNotificationSettings = false
                notificationSettingsLoaded = true
            }
        }
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

    private suspend fun updateScheduledGoalReminders() {
        if (!goalAlertEnabled) {
            GoalNotificationScheduler.cancelGoalReminders(getApplication())
            return
        }

        val uid = authRepository.getCurrentUid() ?: return
        val goals = goalRepository.getGoals(uid)

        GoalNotificationScheduler.scheduleGoalReminders(
            context = getApplication(),
            goals = goals,
            hour = notificationHour,
            minute = notificationMinute
        )
    }

    private suspend fun saveNotificationSettingsToDb() {
        try {
            if (!notificationSettingsLoaded) return

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
            android.util.Log.d(
                "SettingsFirestore",
                "알림 설정 DB 저장 성공 uid=$uid enabled=$notificationEnabled time=$notificationHour:$notificationMinute"
            )
        } catch (e: Exception) {
            android.util.Log.e("SettingsFirestore", "알림 설정 저장 실패", e)
        }
    }
}
