package com.example.studyapp.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var subjects by mutableStateOf(listOf<SubjectTimer>())
        private set

    var studiedMinutes by mutableStateOf(0)
        private set

    // 현재 선택된 과목 id (일시정지 상태여도 유지)
    var selectedTaskId by mutableStateOf<Long?>(null)
        private set

    // 실제 실행 중인 과목 id
    var runningTaskId by mutableStateOf<Long?>(null)
        private set

    private var timerJob: Job? = null

    private var nextId by mutableLongStateOf(1L)

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        runningTaskId = null
        selectedTaskId = null
        studiedMinutes = 0

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

        // 같은 과목을 누른 경우
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

        // 다른 과목 선택
        if (target.remainingSeconds <= 0) return

        selectedTaskId = subjectId
        startTask(subjectId)
    }

    private fun startTask(subjectId: Long) {
        timerJob?.cancel()
        selectedTaskId = subjectId
        runningTaskId = subjectId

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

                val updated = subjects.firstOrNull { it.id == currentId }
                if (updated == null || updated.remainingSeconds <= 0) {
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

        // 선택은 유지하고, 실행만 멈춤
        runningTaskId = null
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}