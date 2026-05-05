package com.example.studyapp.ui.timer.pomodoro

fun buildSingleSubjectSegment(
    allocatedSeconds: Int,
    remainingSeconds: Int,
    taskId: Long = 0L,
    label: String = ""
): List<CircularSegmentUi> {
    if (allocatedSeconds <= 0) return emptyList()

    val progress = remainingSeconds.toFloat() / allocatedSeconds.toFloat()

    return listOf(
        CircularSegmentUi(
            taskId = taskId,
            label = label,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f)
        )
    )
}

fun formatHoursMinutes(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    return "${hours}H ${minutes}M"
}

fun formatCountdown(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}