package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.model.GeneratedScheduleItem
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GeneratedScheduleRepository
import com.example.studyapp.data.repository.ScheduleRepository
import com.example.studyapp.data.repository.SubjectRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class GeneratedScheduleViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val subjectRepository = SubjectRepository()
    private val fixedScheduleRepository = ScheduleRepository()
    private val generatedScheduleRepository = GeneratedScheduleRepository()

    val schedules = mutableStateListOf<DayScheduleBlock>()

    var isGenerating by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.getCurrentUid()
            ?: authRepository.signInAnonymouslyIfNeeded()

        userRepository.ensureUserDocument(
            uid = uid,
            isGuest = authRepository.isCurrentUserAnonymous()
        )

        return uid
    }

    fun clearMessage() {
        message = null
    }

    fun loadSchedules(date: LocalDate) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                loadSchedulesInternal(uid, date)
            } catch (e: Exception) {
                message = e.message ?: "시간표를 불러오지 못했습니다."
            }
        }
    }

    fun generateAndSaveSchedule(date: LocalDate) {
        if (isGenerating) return

        viewModelScope.launch {
            isGenerating = true
            message = null

            try {
                val uid = getOrCreateUid()

                val profile = userRepository.getUserProfile(uid)
                    ?: throw IllegalStateException("사용자 생활패턴 정보가 없습니다.")

                if (profile.wakeTime.isBlank() || profile.sleepTime.isBlank() ||
                    profile.lunchStartTime.isBlank() || profile.lunchEndTime.isBlank() || //추가
                    profile.dinnerStartTime.isBlank() || profile.dinnerEndTime.isBlank()  //추가
                    ) { throw IllegalStateException("기상/취침 시간 및 점심/저녁 시간을 모두 입력해주세요.")
                }

                val subjects = subjectRepository.getSubjects(uid)

                if (subjects.isEmpty()) {
                    throw IllegalStateException("과목을 먼저 추가해주세요.")
                }

                val fixedSchedules = fixedScheduleRepository.getSchedules(uid)

                val generatedSchedules = generatePriorityStudySchedule(
                    date = date,
                    subjects = subjects,
                    fixedSchedules = fixedSchedules,
                    wakeTime = profile.wakeTime,
                    sleepTime = profile.sleepTime,
                    lunchStartTime = profile.lunchStartTime, // 점심 시작
                    lunchEndTime = profile.lunchEndTime,     // 점심 끝
                    dinnerStartTime = profile.dinnerStartTime, // 저녁 시작
                    dinnerEndTime = profile.dinnerEndTime      // 저녁 끝
                )

                if (generatedSchedules.isEmpty()) {
                    throw IllegalStateException("저장할 수 있는 빈 시간이 없습니다.")
                }

                generatedScheduleRepository.replaceSchedulesForDate(
                    userId = uid,
                    date = date.toString(),
                    schedules = generatedSchedules
                )

                loadSchedulesInternal(uid, date)

                message = "시간표를 생성해서 저장했습니다."
            } catch (e: Exception) {
                message = e.message ?: "시간표 생성에 실패했습니다."
            } finally {
                isGenerating = false
            }
        }
    }

    private suspend fun loadSchedulesInternal(uid: String, date: LocalDate) {
        val result = generatedScheduleRepository.getSchedulesByDate(
            userId = uid,
            date = date.toString()
        )

        schedules.clear()

        schedules.addAll(
            result.mapNotNull { item ->
                item.toDayScheduleBlockOrNull(date)
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun GeneratedScheduleItem.toDayScheduleBlockOrNull(date: LocalDate): DayScheduleBlock? {
    val start = startTime.split(":")
    val end = endTime.split(":")

    if (start.size != 2 || end.size != 2) return null

    val startHour = start[0].toIntOrNull() ?: return null
    val startMinute = start[1].toIntOrNull() ?: return null
    val endHour = end[0].toIntOrNull() ?: return null
    val endMinute = end[1].toIntOrNull() ?: return null

    return DayScheduleBlock(
        date = date,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        subject = title,
        color = if (colorArgb != 0) Color(colorArgb) else Color(0xFFBBDEFB)
    )
}