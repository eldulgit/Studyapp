package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun generateLabels(period: StatsPeriod): List<String> {
    val today = LocalDate.now()

    return when (period) {
        StatsPeriod.DAILY -> (0..3).map {
            "${today.minusDays(it.toLong()).dayOfMonth}일"
        }.reversed()

        StatsPeriod.WEEKLY -> generateWeeklyRanges()

        StatsPeriod.MONTHLY -> (0..3).map {
            "${today.minusMonths(it.toLong()).monthValue}월"
        }.reversed()
    }
}
