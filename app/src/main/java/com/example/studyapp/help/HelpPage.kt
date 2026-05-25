package com.example.studyapp.help

data class HelpPage(
    val title: String,
    val description: String,
    val type: HelpPageType
)

enum class HelpPageType {
    Lifestyle,
    ScheduleSetup,
    Timer,
    Stats
}
