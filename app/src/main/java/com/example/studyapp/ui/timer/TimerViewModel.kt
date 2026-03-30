package com.example.studyapp.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.ui.stats.StudySessionRecord
import com.example.studyapp.ui.stats.StudySessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var subjects by mutableStateOf(listOf<SubjectTimer>())
        private set

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

    private fun finishCurrentSessionAndSave() {
        val runningId = runningTaskId ?: selectedTaskId
        val startMillis = currentSessionStartMillis
        val studiedSeconds = currentSessionStudiedSeconds

        if (runningId == null || startMillis == null || studiedSeconds <= 0) {
            currentSessionStartMillis = null
            currentSessionStudiedSeconds = 0
            return
        }

        val subjectName = subjects.firstOrNull { it.id == runningId }?.name ?: "알 수 없는 과목"
        val endMillis = startMillis + (studiedSeconds * 1000L)

        StudySessionRepository.addRecord(
            StudySessionRecord(
                id = nextRecordId++,
                subjectName = subjectName,
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                studiedSeconds = studiedSeconds
            )
        )

        currentSessionStartMillis = null
        currentSessionStudiedSeconds = 0
    }

    override fun onCleared() {
        finishCurrentSessionAndSave()
        timerJob?.cancel()
        super.onCleared()
    }
}