package com.example.studyapp.ui.timer.pomodoro

data class CircularSegmentUi(
    val taskId: Long,
    val label: String,
    val startAngle: Float,
    val sweepAngle: Float
)

data class PomodoroTask(
    val id: Long,
    val subjectName: String,
    val allocatedMinutes: Int,
    val colorArgb: Int,
    val remainingSeconds: Int = allocatedMinutes * 60
)

data class PomodoroUiState(
    val tasks: List<PomodoroTask> = emptyList(),
    val isRunning: Boolean = false,
    val currentTaskId: Long? = null
)