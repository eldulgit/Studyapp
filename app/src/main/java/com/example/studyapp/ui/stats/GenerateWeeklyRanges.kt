package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@RequiresApi(Build.VERSION_CODES.O)
fun generateWeeklyRanges(): List<String> {
    val today = LocalDate.now()
    val currentWeekStart = today.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
    )

    return (3 downTo 0).map { weeksAgo ->
        val start = currentWeekStart.minusWeeks(weeksAgo.toLong())
        val end = start.plusDays(6)

        formatWeeklyRange(start, end)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatWeeklyRange(
    start: LocalDate,
    end: LocalDate
): String {
    return "${start.dayOfMonth}일-${end.dayOfMonth}일"
}
