package com.example.studyapp.ui.settings.schedule

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GoalRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val repository = GoalRepository()

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
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "불러오기 실패", e)
            }
        }
    }

    fun addGoal(
        title: String,
        startDate: String,
        endDate: String,
        pageCount: Int
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.addGoal(uid, title, startDate, endDate, pageCount)
                loadGoalsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "저장 실패", e)
            }
        }
    }

    fun updateGoal(
        id: String,
        title: String,
        startDate: String,
        endDate: String,
        pageCount: Int
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.updateGoal(uid, id, title, startDate, endDate, pageCount)
                loadGoalsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("GoalFirestore", "수정 실패", e)
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
                android.util.Log.e("GoalFirestore", "삭제 실패", e)
            }
        }
    }
}