package com.example.studyapp.ui.timer.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        PomodoroUiState(
            tasks = listOf(
                PomodoroTask(id = 1L, subjectName = "국어", allocatedMinutes = 30),
                PomodoroTask(id = 2L, subjectName = "영어", allocatedMinutes = 20),
                PomodoroTask(id = 3L, subjectName = "수학", allocatedMinutes = 10)
            ),
            isRunning = false,
            currentTaskId = 1L
        )
    )
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun addTask(subjectName: String, minutes: Int) {
        val newTask = PomodoroTask(
            id = System.currentTimeMillis(),
            subjectName = subjectName,
            allocatedMinutes = minutes
        )

        val state = _uiState.value
        val newTasks = state.tasks + newTask

        _uiState.value = state.copy(
            tasks = newTasks,
            currentTaskId = state.currentTaskId ?: newTask.id
        )
    }

    fun start() {
        val state = _uiState.value
        if (state.isRunning) return

        val activeId = state.currentTaskId
            ?: state.tasks.firstOrNull { it.remainingSeconds > 0 }?.id
            ?: return

        _uiState.value = state.copy(
            isRunning = true,
            currentTaskId = activeId
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)

                val currentState = _uiState.value
                if (!currentState.isRunning) break

                val currentId = currentState.currentTaskId
                    ?: currentState.tasks.firstOrNull { it.remainingSeconds > 0 }?.id

                if (currentId == null) {
                    _uiState.value = currentState.copy(
                        isRunning = false,
                        currentTaskId = null
                    )
                    break
                }

                val updatedTasks = currentState.tasks.map { task ->
                    if (task.id == currentId) {
                        task.copy(
                            remainingSeconds = (task.remainingSeconds - 1).coerceAtLeast(0)
                        )
                    } else {
                        task
                    }
                }

                val currentTask = updatedTasks.firstOrNull { it.id == currentId }
                val nextTaskId = when {
                    currentTask == null -> null
                    currentTask.remainingSeconds > 0 -> currentId
                    else -> updatedTasks.firstOrNull { it.remainingSeconds > 0 }?.id
                }

                _uiState.value = currentState.copy(
                    tasks = updatedTasks,
                    currentTaskId = nextTaskId,
                    isRunning = nextTaskId != null
                )
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null

        val resetTasks = _uiState.value.tasks.map { task ->
            task.copy(remainingSeconds = task.allocatedMinutes * 60)
        }

        _uiState.value = _uiState.value.copy(
            tasks = resetTasks,
            isRunning = false,
            currentTaskId = resetTasks.firstOrNull()?.id
        )
    }

    fun skipCurrent() {
        val state = _uiState.value
        val currentId = state.currentTaskId ?: return

        val updatedTasks = state.tasks.map { task ->
            if (task.id == currentId) {
                task.copy(remainingSeconds = 0)
            } else {
                task
            }
        }

        val nextTaskId = updatedTasks.firstOrNull { it.remainingSeconds > 0 }?.id

        _uiState.value = state.copy(
            tasks = updatedTasks,
            currentTaskId = nextTaskId,
            isRunning = nextTaskId != null && state.isRunning
        )

        if (nextTaskId == null) {
            timerJob?.cancel()
            timerJob = null
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}