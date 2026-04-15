package com.example.studyapp.ui.settings.schedule

import android.R

data class ScheduleItem(
    val id: String,
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)