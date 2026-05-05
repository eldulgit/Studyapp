package com.example.studyapp.ui.settings.schedule

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.ScheduleRepository
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }
    private val repository = ScheduleRepository()
    val schedules = mutableStateListOf<ScheduleItem>()

    fun loadSchedulesFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val result = repository.getSchedules(uid)

                schedules.clear()
                schedules.addAll(result)
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "불러오기 실패", e)
            }
        }
    }

    fun addSchedule(
        title: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()

                repository.addSchedule(
                    userId = uid,
                    title = title,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
                loadSchedulesFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "저장 실패", e)
            }
        }
    }

    fun deleteSchedule(id: String) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                repository.deleteSchedule(uid, id)

                loadSchedulesFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "삭제 실패", e)
            }
        }
    }

    fun updateSchedule(
        id: String,
        title: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()

                repository.updateSchedule(
                    userId = uid,
                    id = id,
                    title = title,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
                loadSchedulesFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "수정 실패", e)
            }
        }
    }
}