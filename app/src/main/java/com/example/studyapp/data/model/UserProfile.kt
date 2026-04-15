package com.example.studyapp.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val isGuest: Boolean = true,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)