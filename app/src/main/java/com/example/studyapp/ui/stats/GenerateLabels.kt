package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun generateLabels(period: StatsPeriod): List<String> {

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MM.dd")

    return when (period) {

        StatsPeriod.DAILY -> (0..3).map {
            today.minusDays(it.toLong()).format(formatter)
        }.reversed()

        StatsPeriod.WEEKLY -> generateWeeklyRanges()

        StatsPeriod.MONTHLY -> (0..3).map {
            today.minusMonths(it.toLong()).monthValue.toString() + "월"
        }.reversed()
    }
}

