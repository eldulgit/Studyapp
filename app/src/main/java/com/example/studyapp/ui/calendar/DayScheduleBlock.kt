package com.example.studyapp.ui.calendar

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class DayScheduleBlock(
    val date: LocalDate,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val subject: String,
    val color: Color
)