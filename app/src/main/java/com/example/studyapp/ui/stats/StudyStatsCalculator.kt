package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentPeriodStudySeconds(
    records: List<StudySessionRecord>,
    period: StatsPeriod
): Int {
    val today = LocalDate.now()

    return records.filter { record ->
        val date = record.sessionDate.toLocalDateOrNull() ?: return@filter false

        when (period) {
            StatsPeriod.DAILY -> {
                date == today
            }

            StatsPeriod.WEEKLY -> {
                val start = today.minusDays(6)
                !date.isBefore(start) && !date.isAfter(today)
            }

            StatsPeriod.MONTHLY -> {
                date.year == today.year && date.monthValue == today.monthValue
            }
        }
    }.sumOf { it.studiedSeconds }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getPreviousPeriodStudySeconds(
    records: List<StudySessionRecord>,
    period: StatsPeriod
): Int {
    val today = LocalDate.now()

    return records.filter { record ->
        val date = record.sessionDate.toLocalDateOrNull() ?: return@filter false

        when (period) {
            StatsPeriod.DAILY -> {
                date == today.minusDays(1)
            }

            StatsPeriod.WEEKLY -> {
                val start = today.minusDays(13)
                val end = today.minusDays(7)

                !date.isBefore(start) && !date.isAfter(end)
            }

            StatsPeriod.MONTHLY -> {
                val previousMonth = today.minusMonths(1)

                date.year == previousMonth.year &&
                        date.monthValue == previousMonth.monthValue
            }
        }
    }.sumOf { it.studiedSeconds }
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        null
    }
}