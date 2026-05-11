package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.studyapp.data.model.GeneratedScheduleItem
import com.example.studyapp.ui.settings.schedule.ScheduleItem
import com.example.studyapp.ui.settings.subject.SubjectItem
import java.time.DayOfWeek
import java.time.LocalDate

private data class TimeRange(
    val start: Int,
    val end: Int
) {
    val duration: Int
        get() = end - start
}

private data class SubjectAllocation(
    val subject: SubjectItem,
    var remainingMinutes: Int
)

@RequiresApi(Build.VERSION_CODES.O)
fun generatePriorityStudySchedule(
    date: LocalDate,
    subjects: List<SubjectItem>,
    fixedSchedules: List<ScheduleItem>,
    wakeTime: String,
    sleepTime: String,
    lunchStartTime: String,
    lunchEndTime: String,
    dinnerStartTime: String,
    dinnerEndTime: String
): List<GeneratedScheduleItem> {
    if (subjects.isEmpty()) return emptyList()

    val wakeMinutes = parseTimeToMinutes(wakeTime)
    val sleepMinutes = parseTimeToMinutes(sleepTime)

    if (wakeMinutes == null || sleepMinutes == null) {
        throw IllegalArgumentException("기상 시간 또는 취침 시간 형식이 올바르지 않습니다.")
    }

    val studyRanges = createAwakeRanges(
        wakeMinutes = wakeMinutes,
        sleepMinutes = sleepMinutes
    )

    val todayKoreanDay = date.toKoreanDayOfWeek()

    val todayFixedRanges = fixedSchedules
        .filter { it.dayOfWeek == todayKoreanDay }
        .mapNotNull { schedule ->
            val start = parseTimeToMinutes(schedule.startTime)
            val end = parseTimeToMinutes(schedule.endTime)

            if (start == null || end == null || start >= end) {
                null
            } else {
                TimeRange(start, end)
            }
        }.sortedBy { it.start }

    val mealRanges = mutableListOf<TimeRange>()
    val lunchStartMins = parseTimeToMinutes(lunchStartTime)
    val lunchEndMins = parseTimeToMinutes(lunchEndTime)
    if (lunchStartMins != null && lunchEndMins != null && lunchStartMins < lunchEndMins) {
        mealRanges.add(TimeRange(lunchStartMins, lunchEndMins))
    }

    val dinnerStartMins = parseTimeToMinutes(dinnerStartTime)
    val dinnerEndMins = parseTimeToMinutes(dinnerEndTime)
    if (dinnerStartMins != null && dinnerEndMins != null && dinnerStartMins < dinnerEndMins) {
        mealRanges.add(TimeRange(dinnerStartMins, dinnerEndMins))
    }
    val allUnavailableRanges = (todayFixedRanges + mealRanges).sortedBy { it.start }

    val freeRanges = subtractFixedSchedules(
        awakeRanges = studyRanges,
        fixedRanges = allUnavailableRanges
    ).filter { it.duration >= 30 }

    val totalFreeMinutes = freeRanges.sumOf { it.duration }

    if (totalFreeMinutes <= 0) return emptyList()

    val totalPriority = subjects.sumOf { subject ->
        subject.priority.coerceAtLeast(1)
    }

    val allocations = subjects
        .sortedByDescending { it.priority }
        .map { subject ->
            val minutes = (totalFreeMinutes * subject.priority.coerceAtLeast(1) / totalPriority / 10) * 10

            SubjectAllocation(
                subject = subject,
                remainingMinutes = minutes
            )
        }.toMutableList()

    val generatedSchedules = mutableListOf<GeneratedScheduleItem>()


    var allocationIndex = 0

    for (freeRange in freeRanges) {
        var current = freeRange.start

        while (current < freeRange.end && allocationIndex < allocations.size) {
            val currentSubject = allocations[allocationIndex]

            if (currentSubject.remainingMinutes <= 0) {
                allocationIndex++
                continue
            }

            val availableMinutes = freeRange.end - current

            val studyMinutes = (minOf(currentSubject.remainingMinutes, availableMinutes) / 10) * 10

            if (studyMinutes < 20) {
                if (currentSubject.remainingMinutes < 20) {
                    currentSubject.remainingMinutes = 0
                    allocationIndex++
                } else {
                    // 현재 빈 칸이 너무 작으면 이 시간대는 건너뛰고 다음 freeRange로 이동
                    current = freeRange.end
                }
                continue
            }

            val start = current
            val end = current + studyMinutes

            generatedSchedules.add(
                GeneratedScheduleItem(
                    date = date.toString(),
                    title = currentSubject.subject.name,
                    startTime = formatMinutesToTime(start),
                    endTime = formatMinutesToTime(end),
                    subjectId = currentSubject.subject.id,
                    colorArgb = currentSubject.subject.colorArgb,
                    priority = currentSubject.subject.priority,
                    isCompleted = false
                )
            )
            current = end
            currentSubject.remainingMinutes -= studyMinutes

            if (currentSubject.remainingMinutes <= 0) {
                allocationIndex++
            }
        }
    }
    return generatedSchedules.sortedBy { it.startTime }
}

private fun parseTimeToMinutes(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    val totalMinutes = hour * 60 + minute
    return (totalMinutes / 10) * 10
}

private fun formatMinutesToTime(minutes: Int): String {
    val fixedMinutes = minutes.coerceIn(0, 24 * 60 - 1)
    val hour = fixedMinutes / 60
    val minute = fixedMinutes % 60

    return "%02d:%02d".format(hour, minute)
}

private fun createAwakeRanges(
    wakeMinutes: Int,
    sleepMinutes: Int
): List<TimeRange> {
    return if (wakeMinutes < sleepMinutes) {
        listOf(
            TimeRange(wakeMinutes, sleepMinutes)
        )
    } else {
        listOf(
            TimeRange(0, sleepMinutes),
            TimeRange(wakeMinutes, 24 * 60)
        )
    }
}

private fun subtractFixedSchedules(
    awakeRanges: List<TimeRange>,
    fixedRanges: List<TimeRange>
): List<TimeRange> {
    val result = mutableListOf<TimeRange>()

    for (awakeRange in awakeRanges) {
        var currentStart = awakeRange.start

        val relatedFixedRanges = fixedRanges
            .filter { fixed ->
                fixed.start < awakeRange.end && fixed.end > awakeRange.start
            }
            .sortedBy { it.start }

        for (fixedRange in relatedFixedRanges) {
            val fixedStart = fixedRange.start.coerceAtLeast(awakeRange.start)
            val fixedEnd = fixedRange.end.coerceAtMost(awakeRange.end)

            if (currentStart < fixedStart) {
                result.add(
                    TimeRange(
                        start = currentStart,
                        end = fixedStart
                    )
                )
            }
            currentStart = maxOf(currentStart, fixedEnd)
        }
        if (currentStart < awakeRange.end) {
            result.add(
                TimeRange(
                    start = currentStart,
                    end = awakeRange.end
                )
            )
        }
    }
    return result
}

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.toKoreanDayOfWeek(): String {
    return when (this.dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
}