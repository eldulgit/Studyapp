package com.example.studyapp.ui.stats

data class StudySessionRecord(
    val id: Long,
    val subjectName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val studiedSeconds: Int
)