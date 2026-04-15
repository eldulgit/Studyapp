package com.example.studyapp.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.ui.stats.StudySessionRepository
import com.example.studyapp.data.repository.AuthRepository
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
        loadSubjectsFromFirestore()
    }

    var studiedMinutes by mutableStateOf(0)
        private set

    var selectedTaskId by mutableStateOf<Long?>(null)
        private set

    var runningTaskId by mutableStateOf<Long?>(null)
        private set

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
                        remainingSeconds = 0
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
    }

    fun updateSubjectTime(subjectId: Long, hour: String, minute: String) {
        val hourInt = hour.toIntOrNull() ?: 0
        val minuteInt = minute.toIntOrNull() ?: 0
        val totalSeconds = (hourInt * 60 + minuteInt) * 60

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
    }

    fun toggleTask(subjectId: Long) {
        val target = subjects.firstOrNull { it.id == subjectId } ?: return
        if (target.allocatedSeconds <= 0) return

        if (selectedTaskId == subjectId) {
            if (runningTaskId == subjectId) {
                pause()
            } else {
                if (target.remainingSeconds > 0) {
                    startTask(subjectId)
                }
            }
            return
        }

        if (target.remainingSeconds <= 0) return

        if (runningTaskId != null) {
            finishCurrentSessionAndSave()
            timerJob?.cancel()
            timerJob = null
            runningTaskId = null
        }

        selectedTaskId = subjectId
        startTask(subjectId)
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

    fun finishCurrentSessionAndSave() {
        val currentId = runningTaskId ?: return
        val currentSubject = subjects.firstOrNull { it.id == currentId } ?: return
        val subjectName = currentSubject.name

        val startTime = currentSessionStartMillis ?: return
        val endTime = System.currentTimeMillis()
        val studiedSeconds = currentSessionStudiedSeconds
        if (studiedSeconds <= 0) return

        val sessionDate = makeSessionDate(startTime)

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

                currentSessionStartMillis = null
                currentSessionStudiedSeconds = 0
            } catch (e: Exception) {
                android.util.Log.e("TimerFirestore", "공부 기록 저장 실패", e)
            }
        }
    }

    override fun onCleared() {
        finishCurrentSessionAndSave()
        timerJob?.cancel()
        super.onCleared()
    }
}