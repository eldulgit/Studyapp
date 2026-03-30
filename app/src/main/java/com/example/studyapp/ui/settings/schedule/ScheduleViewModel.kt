package com.example.studyapp.ui.schedule

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.ScheduleRepository
import com.example.studyapp.ui.settings.schedule.ScheduleItem
import kotlinx.coroutines.launch
import com.example.studyapp.data.repository.AuthRepository

class ScheduleViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private fun getUidOrNull(): String? {
        return authRepository.getCurrentUid()
    }
    private val repository = ScheduleRepository()
    val schedules = mutableStateListOf<ScheduleItem>()

    fun loadSchedulesFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch
                val result = repository.getSchedules(uid)

                schedules.clear()
                schedules.addAll(result)
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "불러오기 실패", e)
            }
        }
    }

    fun addSchedule(
        subjectId: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch

                repository.addSchedule(
                    userId = uid,
                    subjectId = subjectId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    type = type
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
                val uid = getUidOrNull() ?: return@launch
                repository.deleteSchedule(uid, id)

                loadSchedulesFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "삭제 실패", e)
            }
        }
    }

    fun updateSchedule(
        id: String,
        subjectId: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch

                repository.updateSchedule(
                    userId = uid,
                    id = id,
                    subjectId = subjectId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    type = type
                )
                loadSchedulesFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("ScheduleFirestore", "수정 실패", e)
            }
        }
    }
}