package com.example.studyapp.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val isGuest: Boolean = true,

    val wakeTime: String = "",
    val sleepTime: String = "",
    val lunchStartTime: String = "",
    val lunchEndTime: String = "",
    val dinnerStartTime: String = "",
    val dinnerEndTime: String = "",
    val lifestyleCompleted: Boolean = false,

    val createdAt: Any? = null,
    val updatedAt: Any? = null
)