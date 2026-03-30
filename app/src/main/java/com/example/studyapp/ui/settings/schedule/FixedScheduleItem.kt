package com.example.studyapp.ui.settings.schedule

data class FixedScheduleItem(
    val id: Long,
    val category: ScheduleCategory,
    val title: String,

    // 목표용
    val startDate: String? = null,
    val endDate: String? = null,
    val pageCount: Int? = null,

    // 스케줄용
    val dayOfWeek: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)