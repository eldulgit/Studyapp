package com.example.studyapp.data.model

data class Holiday(
    val date: String = "",
    val localName: String = "",
    val name: String = "",
    val countryCode: String = "",
    val global: Boolean = false
)

//공휴일 받아올 변수 선언