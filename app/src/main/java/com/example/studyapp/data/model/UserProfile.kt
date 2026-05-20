package com.example.studyapp.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val isGuest: Boolean = true,

    val wakeTime: String = "",
    val sleepTime: String = "",
    val lunchStartTime: String = "",
    val lunchEndTime: String = "",
    val dinnerStartTime: String = "",
    val dinnerEndTime: String = "",
    val lifestyleCompleted: Boolean = false,

    val notificationEnabled: Boolean = true,
    val notificationHour: String = "08",
    val notificationMinute: String = "00",

    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
