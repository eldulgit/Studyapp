package com.example.studyapp.ui.settings.goal

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.GoalRepository
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GoalRepository(application)

    var dailyGoalMinutes by mutableStateOf(240)
        private set

    var weeklyGoalMinutes by mutableStateOf(1200)
        private set

    var monthlyGoalMinutes by mutableStateOf(3000)
        private set

    init {
        viewModelScope.launch {
            repo.getDailyGoal().collect { dailyGoalMinutes = it }
        }
        viewModelScope.launch {
            repo.getWeeklyGoal().collect { weeklyGoalMinutes = it }
        }
        viewModelScope.launch {
            repo.getMonthlyGoal().collect { monthlyGoalMinutes = it }
        }
    }

    fun updateDailyGoal(minutes: Int) {
        dailyGoalMinutes = minutes
        viewModelScope.launch { repo.saveDailyGoal(minutes) }
    }

    fun updateWeeklyGoal(minutes: Int) {
        weeklyGoalMinutes = minutes
        viewModelScope.launch { repo.saveWeeklyGoal(minutes) }
    }

    fun updateMonthlyGoal(minutes: Int) {
        monthlyGoalMinutes = minutes
        viewModelScope.launch { repo.saveMonthlyGoal(minutes) }
    }
}
