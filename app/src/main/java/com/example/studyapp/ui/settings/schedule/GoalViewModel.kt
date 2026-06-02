package com.example.studyapp.ui.settings.schedule

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GoalRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.notification.GoalNotificationScheduler
import com.example.studyapp.ui.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val repository = GoalRepository()
    private val settingsRepository = SettingsRepository(application)

    val goals = mutableStateListOf<GoalItem>()

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    fun loadGoalsFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val result = repository.getGoals(uid)
                goals.clear()
                goals.addAll(result)
                refreshGoalRemindersIfEnabled(result)
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "목표 불러오기 실패", e)
            }
        }
    }

    fun addGoal(
        title: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.addGoal(
                    userId = uid,
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    increasePriorityOverTime = false
                )
                loadGoalsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "목표 저장 실패", e)
            }
        }
    }

    fun updateGoal(
        id: String,
        title: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.updateGoal(uid, id, title, startDate, endDate)
                loadGoalsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "목표 수정 실패", e)
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.deleteGoal(uid, id)
                loadGoalsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "목표 삭제 실패", e)
            }
        }
    }

    fun updateGoalPriorityIncrease(
        id: String,
        increasePriorityOverTime: Boolean
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()

                repository.updateGoalPriorityIncrease(
                    userId = uid,
                    id = id,
                    increasePriorityOverTime = increasePriorityOverTime
                )

                val index = goals.indexOfFirst { it.id == id }
                if (index != -1) {
                    goals[index] = goals[index].copy(
                        increasePriorityOverTime = increasePriorityOverTime
                    )
                }
                refreshGoalRemindersIfEnabled(goals)
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "목표 체크 상태 저장 실패", e)
            }
        }
    }

    private suspend fun refreshGoalRemindersIfEnabled(goalItems: List<GoalItem>) {
        if (!settingsRepository.goalAlertEnabledFlow.first()) {
            GoalNotificationScheduler.cancelGoalReminders(getApplication())
            return
        }

        GoalNotificationScheduler.scheduleGoalReminders(
            context = getApplication(),
            goals = goalItems,
            hour = settingsRepository.notificationHourFlow.first(),
            minute = settingsRepository.notificationMinuteFlow.first()
        )
    }
}
