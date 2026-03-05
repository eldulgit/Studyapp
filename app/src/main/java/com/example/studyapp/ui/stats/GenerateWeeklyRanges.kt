package com.example.studyapp.ui.stats

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun generateWeeklyRanges(): List<String> {

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d")

    return (0..3).map { block ->

        val end = today.minusDays((3 - block) * 7L)
        val start = end.minusDays(6)

        "${start.format(formatter)}-${end.format(formatter)}"
    }
}
