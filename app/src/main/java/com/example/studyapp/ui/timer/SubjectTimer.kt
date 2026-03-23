package com.example.studyapp.ui.timer

data class SubjectTimer(
    val id: Long,
    val name: String,
    val allocatedSeconds: Int,
    val remainingSeconds: Int,
)