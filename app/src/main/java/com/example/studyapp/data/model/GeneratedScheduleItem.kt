package com.example.studyapp.data.model

data class GeneratedScheduleItem(
    val id: String = "",
    val date: String = "",
    val title: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val subjectId: String? = null,
    val colorArgb: Int = 0,
    val priority: Int = 1,
    val isCompleted: Boolean = false
)