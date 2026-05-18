package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.studyapp.util.AppTimeZone
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentPeriodStudySeconds(
    records: List<StudySessionRecord>,
    period: StatsPeriod
): Int {
    val today = LocalDate.now(AppTimeZone.zoneId)

    return records.filter { record ->
        val date = record.sessionDate.toLocalDateOrNull() ?: return@filter false

        when (period) {
            StatsPeriod.DAILY -> {
                date == today
            }

            StatsPeriod.WEEKLY -> {
                val start = today.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
                )
                val end = start.plusDays(6)

                !date.isBefore(start) && !date.isAfter(end)
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
    val today = LocalDate.now(AppTimeZone.zoneId)

    return records.filter { record ->
        val date = record.sessionDate.toLocalDateOrNull() ?: return@filter false

        when (period) {
            StatsPeriod.DAILY -> {
                date == today.minusDays(1)
            }

            StatsPeriod.WEEKLY -> {
                val currentWeekStart = today.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
                )
                val start = currentWeekStart.minusWeeks(1)
                val end = start.plusDays(6)

                !date.isBefore(start) && !date.isAfter(end)
            }

            StatsPeriod.MONTHLY -> {
                val previousMonth = today.minusMonths(1)
                date.year == previousMonth.year && date.monthValue == previousMonth.monthValue
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
