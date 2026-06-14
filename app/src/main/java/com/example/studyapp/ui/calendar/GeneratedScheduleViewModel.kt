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
import com.example.studyapp.data.repository.GoalRepository
import com.example.studyapp.data.repository.ScheduleRepository
import com.example.studyapp.data.repository.SubjectRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.ui.settings.schedule.GoalItem // 추가
import com.example.studyapp.ui.settings.schedule.ScheduleItem
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class GeneratedScheduleViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val subjectRepository = SubjectRepository()
    private val fixedScheduleRepository = ScheduleRepository()
    private val generatedScheduleRepository = GeneratedScheduleRepository()

    val schedules = mutableStateListOf<DayScheduleBlock>()
    val fixedScheduleBlocks = mutableStateListOf<DayScheduleBlock>()

    var isGenerating by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var wakeTime by mutableStateOf("07:00")
        private set
    var sleepTime by mutableStateOf("23:00")
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

                val profile = userRepository.getUserProfile(uid)
                profile?.let {
                    if (it.wakeTime.isNotBlank()) wakeTime = it.wakeTime
                    if (it.sleepTime.isNotBlank()) sleepTime = it.sleepTime
                }
                updateFixedScheduleBlocks(
                    date = date,
                    fixedSchedules = fixedScheduleRepository.getSchedules(uid)
                )
                loadSchedulesInternal(uid, date)
            } catch (e: Exception) {
                message = e.message ?: "시간표를 불러오지 못했습니다."
            }
        }
    }

    private val goalRepository = GoalRepository()
    fun generateAndSaveSchedule(
        date: LocalDate,
        showSuccessMessage: Boolean = true
    ) {
        if (isGenerating) return

        viewModelScope.launch {
            isGenerating = true
            message = null

            try {
                val uid = getOrCreateUid()
                val goals = goalRepository.getGoals(uid)

                val profile = userRepository.getUserProfile(uid)
                    ?: throw IllegalStateException("사용자 생활패턴 정보가 없습니다.")

                if (profile.wakeTime.isNotBlank()) wakeTime = profile.wakeTime
                if (profile.sleepTime.isNotBlank()) sleepTime = profile.sleepTime

                if (profile.wakeTime.isBlank() || profile.sleepTime.isBlank() ||
                    profile.lunchStartTime.isBlank() || profile.lunchEndTime.isBlank() || //추가
                    profile.dinnerStartTime.isBlank() || profile.dinnerEndTime.isBlank()  //추가
                ) { throw IllegalStateException("기상/취침 시간 및 점심/저녁 시간을 모두 입력해주세요.")
                }

                val subjects = subjectRepository.getSubjects(uid)

                if (subjects.isEmpty()  && goals.isEmpty()) {
                    throw IllegalStateException("과목 또는 목표 먼저 추가해주세요.")
                }

                val fixedSchedules = fixedScheduleRepository.getSchedules(uid)
                updateFixedScheduleBlocks(
                    date = date,
                    fixedSchedules = fixedSchedules
                )

                val generatedSchedules = generatePriorityStudySchedule(
                    date = date,
                    subjects = subjects,
                    fixedSchedules = fixedSchedules,
                    goals = goals,
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

                if (showSuccessMessage) {
                    message = "시간표를 생성해서 저장했습니다."
                }
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

    private fun updateFixedScheduleBlocks(
        date: LocalDate,
        fixedSchedules: List<ScheduleItem>
    ) {
        val todayKoreanDay = date.toKoreanDayOfWeek()

        fixedScheduleBlocks.clear()
        fixedScheduleBlocks.addAll(
            fixedSchedules
                .filter { it.dayOfWeek == todayKoreanDay }
                .mapNotNull { schedule -> schedule.toFixedScheduleBlockOrNull(date) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun ScheduleItem.toFixedScheduleBlockOrNull(date: LocalDate): DayScheduleBlock? {
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
        subject = "고정스케줄",
        color = FixedScheduleBlockColor
    )
}

private val FixedScheduleBlockColor = Color(0xFFE5E7EB)

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.toKoreanDayOfWeek(): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
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
        color = Color(resolveScheduleColorArgb(title, colorArgb))
    )
}

private fun resolveScheduleColorArgb(title: String, colorArgb: Int): Int {
    val grayArgb = -7829368

    if (colorArgb != 0 && colorArgb != grayArgb) {
        return colorArgb
    }

    val index = kotlin.math.abs(title.hashCode()) % fallbackScheduleColorPalette.size
    return fallbackScheduleColorPalette[index]
}

private val fallbackScheduleColorPalette = listOf(
    0xFFBDE0FE.toInt(),
    0xFFD0E6FF.toInt(),
    0xFFBFCBFF.toInt(),
    0xFFD9C2F0.toInt(),
    0xFFEADCF8.toInt(),
    0xFFC7E9F1.toInt()
)
