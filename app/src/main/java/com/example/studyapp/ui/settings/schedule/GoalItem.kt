package com.example.studyapp.ui.settings.schedule

data class GoalItem(
    val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val pageCount: Int,
    val increasePriorityOverTime: Boolean = false
)