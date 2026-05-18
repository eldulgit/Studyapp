package com.example.studyapp.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.ui.stats.StudySessionRepository
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GeneratedScheduleRepository
import com.example.studyapp.data.repository.SubjectRepository
import com.example.studyapp.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var subjects by mutableStateOf(listOf<SubjectTimer>())
        private set

    init {
//        loadSubjectsFromFirestore()
    }

    var studiedMinutes by mutableStateOf(0)
        private set

    var selectedTaskId by mutableStateOf<Long?>(null)
        private set

    var runningTaskId by mutableStateOf<Long?>(null)
        private set

    var pausedByCamera by mutableStateOf(false)
        private set
    var todayScheduleTimers by mutableStateOf(listOf<SubjectTimer>())
        private set
    private val generatedScheduleRepository = GeneratedScheduleRepository()
    private var timerJob: Job? = null

    private var nextId by mutableLongStateOf(1L)
    private var nextRecordId by mutableLongStateOf(1L)

    private var totalStudiedSeconds = 0
    private var currentSessionStartMillis: Long? = null
    private var currentSessionStudiedSeconds = 0

    private val subjectRepository = SubjectRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val studySessionRepository = StudySessionRepository()

    fun reset() {
        finishCurrentSessionAndSave()

        timerJob?.cancel()
        timerJob = null
        runningTaskId = null
        selectedTaskId = null
        studiedMinutes = 0
        totalStudiedSeconds = 0

        subjects = subjects.map { subject ->
            subject.copy(remainingSeconds = subject.allocatedSeconds)
        }
    }

    fun startCameraMonitoring() {
        if (runningTaskId == null) return

        pausedByCamera = false

        // 카메라를 켠 시점을 실제 집중 시작 시점으로 저장
        currentSessionStartMillis = System.currentTimeMillis()
        currentSessionStudiedSeconds = 0
    }

    fun loadSubjectsFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val firestoreSubjects = subjectRepository.getSubjects(uid)

                subjects = firestoreSubjects.map { subject ->
                    SubjectTimer(
                        id = subject.id.hashCode().toLong(),
                        name = subject.name,
                        allocatedSeconds = 0,
                        remainingSeconds = 0,
                        colorArgb = subject.colorArgb
                    )
                }

                nextId = (subjects.maxOfOrNull { it.id } ?: 0L) + 1L
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "타이머 과목 불러오기 실패", e)
            }
        }
    }

    fun addSubjectTimer(subjectName: String) {
        val alreadyExists = subjects.any { it.name == subjectName }

        if (alreadyExists) return

        val newSubject = SubjectTimer(
            id = nextId++,
            name = subjectName,
            allocatedSeconds = 0,
            remainingSeconds = 0
        )

        subjects = subjects + newSubject

        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val today = makeSessionDate(System.currentTimeMillis())

                generatedScheduleRepository.saveTimerTimeOverride(
                    userId = uid,
                    date = today,
                    timerId = newSubject.id,
                    subjectName = newSubject.name,
                    allocatedSeconds = newSubject.allocatedSeconds,
                    remainingSeconds = newSubject.remainingSeconds
                )
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "직접 추가한 타이머 과목 저장 실패", e)
            }
        }
    }

    fun updateSubjectTime(subjectId: Long, hour: String, minute: String) {
        val targetSubject = subjects.firstOrNull { it.id == subjectId } ?: return

        val hourInt = hour.toIntOrNull() ?: 0
        val minuteInt = minute.toIntOrNull() ?: 0

        val totalSeconds = ((hourInt * 60) + minuteInt) * 60

        subjects = subjects.map { subject ->
            if (subject.id == subjectId) {
                subject.copy(
                    allocatedSeconds = totalSeconds,
                    remainingSeconds = totalSeconds
                )
            } else {
                subject
            }
        }

        if (selectedTaskId == subjectId && totalSeconds <= 0) {
            pause()
            selectedTaskId = null
        }

        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val today = makeSessionDate(System.currentTimeMillis())

                generatedScheduleRepository.saveTimerTimeOverride(
                    userId = uid,
                    date = today,
                    timerId = subjectId,
                    subjectName = targetSubject.name,
                    allocatedSeconds = totalSeconds,
                    remainingSeconds = totalSeconds
                )
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "타이머 직접 수정값 저장 실패", e)
            }
        }
    }

    fun toggleTask(subjectId: Long) {
        val target = subjects.firstOrNull { it.id == subjectId } ?: return
        if (target.allocatedSeconds <= 0) return
        if (target.remainingSeconds <= 0) return

        if (selectedTaskId == subjectId) {
            // 같은 과목을 다시 누르면 선택 해제 + 실제 타이머 정지
            pause()
            selectedTaskId = null
            pausedByCamera = false
            return
        }

        // 다른 과목을 선택하면 기존 실제 타이머만 정지
        pause()

        // 여기서는 과목 선택만 함
        // 시간을 감소시키는 startTask(subjectId)는 호출하지 않음
        selectedTaskId = subjectId
        pausedByCamera = false
    }

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    private fun makeSessionDate(timeMillis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date(timeMillis))
    }

    private fun startTask(subjectId: Long) {
        timerJob?.cancel()

        selectedTaskId = subjectId
        runningTaskId = subjectId
        currentSessionStartMillis = System.currentTimeMillis()
        currentSessionStudiedSeconds = 0

        timerJob = viewModelScope.launch {
            while (isActive) {
                val currentId = runningTaskId ?: break

                delay(1000)

                val currentSubject = subjects.firstOrNull { it.id == currentId } ?: break
                if (currentSubject.remainingSeconds <= 0) {
                    runningTaskId = null
                    break
                }

                subjects = subjects.map { subject ->
                    if (subject.id == currentId) {
                        subject.copy(
                            remainingSeconds = (subject.remainingSeconds - 1).coerceAtLeast(0)
                        )
                    } else {
                        subject
                    }
                }

                currentSessionStudiedSeconds += 1
                totalStudiedSeconds += 1
                studiedMinutes = totalStudiedSeconds / 60

                val updated = subjects.firstOrNull { it.id == currentId }
                if (updated == null || updated.remainingSeconds <= 0) {
                    finishCurrentSessionAndSave()
                    runningTaskId = null
                    break
                }
            }

            timerJob = null
        }
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null

        finishCurrentSessionAndSave()
        runningTaskId = null
    }

    fun pauseByCamera() {
        if (runningTaskId != null) {
            pausedByCamera = true
        }

        pause()
    }

    fun resumeByCamera() {
        val targetId = selectedTaskId ?: return
        val targetSubject = subjects.firstOrNull { it.id == targetId } ?: return

        if (runningTaskId != null) return
        if (targetSubject.remainingSeconds <= 0) return

        pausedByCamera = false
        startTask(targetId)
    }

    fun stopCameraMonitoring() {
        pausedByCamera = false

        pause()
    }

    fun finishCurrentSessionAndSave() {
        val currentId = runningTaskId ?: return
        val currentSubject = subjects.firstOrNull { it.id == currentId } ?: return

        val subjectName = currentSubject.name
        val startTime = currentSessionStartMillis ?: return
        val endTime = System.currentTimeMillis()
        val studiedSeconds = currentSessionStudiedSeconds

        // 저장할 공부 시간이 없으면 저장하지 않음
        if (studiedSeconds <= 0) {
            currentSessionStartMillis = null
            currentSessionStudiedSeconds = 0
            return
        }

        val sessionDate = makeSessionDate(startTime)

        // 다음 세션과 값이 섞이지 않게 먼저 초기화
        currentSessionStartMillis = null
        currentSessionStudiedSeconds = 0

        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()

                studySessionRepository.addRecord(
                    userId = uid,
                    subjectName = subjectName,
                    startTimeMillis = startTime,
                    endTimeMillis = endTime,
                    studiedSeconds = studiedSeconds,
                    sessionDate = sessionDate
                )
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "공부 기록 저장 실패", e)
            }
        }
    }

    fun loadTodayGeneratedScheduleTimersFromDb() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()

                val today = makeSessionDate(System.currentTimeMillis())

                val generatedSchedules = generatedScheduleRepository.getSchedulesByDate(
                    userId = uid,
                    date = today
                )

                val timerOverrides = generatedScheduleRepository.getTimerTimeOverrides(
                    userId = uid,
                    date = today
                )

                val wakeStartMinute = userRepository
                    .getUserProfile(uid)
                    ?.wakeTime
                    ?.toMinutesOrNull()
                    ?: 0

                val firestoreSubjects = subjectRepository.getSubjects(uid)

                val mergedTimerMap = linkedMapOf<String, SubjectTimer>()
                val firstStartMinuteByKey = mutableMapOf<String, Int>()

                generatedSchedules
                    .filter { schedule ->
                        !schedule.isCompleted &&
                                schedule.title.isNotBlank() &&
                                schedule.startTime.isNotBlank() &&
                                schedule.endTime.isNotBlank()
                    }
                    .forEach { schedule ->
                        val durationSeconds = calculateDurationSeconds(
                            startTime = schedule.startTime,
                            endTime = schedule.endTime
                        )
                        val startMinute = schedule.startTime.toMinutesOrNull()

                        if (durationSeconds <= 0 || startMinute == null) {
                            return@forEach
                        }

                        val matchedSubject = firestoreSubjects.firstOrNull { subject ->
                            subject.id == schedule.subjectId || subject.name == schedule.title
                        }

                        val subjectName = matchedSubject?.name ?: schedule.title.trim()

                        /*
                         * 같은 과목인지 판단하는 기준
                         *
                         * 1순위: DB에 등록된 과목 id
                         * 2순위: 과목 이름
                         *
                         * subjectId가 있는 경우에는 과목 id 기준
                         * subjectId가 없는 경우에는 과목명 기준
                         */
                        val subjectKey = matchedSubject?.id ?: subjectName

                        val existingTimer = mergedTimerMap[subjectKey]
                        val timelineStartMinute = startMinute.normalizeFrom(wakeStartMinute)

                        firstStartMinuteByKey[subjectKey] = minOf(
                            firstStartMinuteByKey[subjectKey] ?: timelineStartMinute,
                            timelineStartMinute
                        )

                        if (existingTimer == null) {
                            mergedTimerMap[subjectKey] = SubjectTimer(
                                id = subjectKey.hashCode().toLong(),
                                name = subjectName,
                                allocatedSeconds = durationSeconds,
                                remainingSeconds = durationSeconds,
                                colorArgb = schedule.colorArgb.takeIf { it != 0 }
                                    ?: matchedSubject?.colorArgb
                            )
                        } else {
                            val totalSeconds = existingTimer.allocatedSeconds + durationSeconds

                            mergedTimerMap[subjectKey] = existingTimer.copy(
                                allocatedSeconds = totalSeconds,
                                remainingSeconds = totalSeconds
                            )
                        }
                    }

                val scheduleTimers = mergedTimerMap
                    .entries
                    .sortedWith(
                        compareBy<Map.Entry<String, SubjectTimer>> {
                            firstStartMinuteByKey[it.key] ?: Int.MAX_VALUE
                        }.thenBy { it.value.name }
                    )
                    .map { (_, timer) ->
                        val override = timerOverrides[timer.id]

                        if (override != null) {
                            timer.copy(
                                allocatedSeconds = override.allocatedSeconds,
                                remainingSeconds = override.remainingSeconds
                            )
                        } else {
                            timer
                        }
                    }

                val validSubjectNames = firestoreSubjects
                    .map { it.name.trim() }
                    .toSet()

                val manualTimers = timerOverrides.values
                    .filter { override ->
                        val overrideSubjectName = override.subjectName.trim()

                        overrideSubjectName.isNotBlank() &&
                                validSubjectNames.contains(overrideSubjectName) &&
                                scheduleTimers.none { timer ->
                                    timer.id == override.timerId || timer.name == overrideSubjectName
                                }
                    }
                    .map { override ->
                        val overrideSubjectName = override.subjectName.trim()

                        val matchedSubject = firestoreSubjects.firstOrNull { subject ->
                            subject.name.trim() == overrideSubjectName
                        }

                        SubjectTimer(
                            id = override.timerId,
                            name = overrideSubjectName,
                            allocatedSeconds = override.allocatedSeconds,
                            remainingSeconds = override.remainingSeconds,
                            colorArgb = matchedSubject?.colorArgb
                        )
                    }
                    .sortedBy { it.name }

                subjects = (scheduleTimers + manualTimers).sortedByDescending { it.allocatedSeconds }

                nextId = (subjects.maxOfOrNull { it.id } ?: 0L) + 1L
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "생성된 오늘 스케줄 타이머 불러오기 실패", e)
            }
        }
    }


    override fun onCleared() {
        finishCurrentSessionAndSave()
        timerJob?.cancel()
        super.onCleared()
    }
}

private fun calculateDurationSeconds(startTime: String, endTime: String): Int {
    val start = startTime.toMinutesOrNull() ?: return 0
    val end = endTime.toMinutesOrNull() ?: return 0

    val diff = (end - start + 1440) % 1440
    return (if (diff == 0 && startTime != endTime) 1440 else diff) * 60
}

private fun String.toMinutesOrNull(): Int? {
    val parts = split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return hour * 60 + minute
}

private fun Int.normalizeFrom(baseStartMinute: Int): Int {
    return if (this < baseStartMinute) {
        this + 24 * 60
    } else {
        this
    }
}
