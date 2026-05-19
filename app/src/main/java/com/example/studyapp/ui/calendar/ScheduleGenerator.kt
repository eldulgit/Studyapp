package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.studyapp.data.model.GeneratedScheduleItem
import com.example.studyapp.ui.settings.schedule.ScheduleItem
import com.example.studyapp.ui.settings.subject.SubjectItem
import com.example.studyapp.ui.settings.schedule.GoalItem
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.compareTo
import kotlin.div
import kotlin.text.toInt
import kotlin.times

private data class TimeRange(
    val start: Int,
    val end: Int
) {
    val duration: Int
        get() = end - start
}

private data class SubjectAllocation(
    val subject: SubjectItem,
    var remainingMinutes: Int,
    val priorityScore: Int = 0 //추가
)

@RequiresApi(Build.VERSION_CODES.O)
fun generatePriorityStudySchedule(
    date: LocalDate,
    subjects: List<SubjectItem>,
    fixedSchedules: List<ScheduleItem>,
    goals: List<GoalItem>, // 추가
    wakeTime: String,
    sleepTime: String,
    lunchStartTime: String,
    lunchEndTime: String,
    dinnerStartTime: String,
    dinnerEndTime: String
): List<GeneratedScheduleItem> {
    if (subjects.isEmpty() && goals.isEmpty()) return emptyList()

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
    ).filter { it.duration > 30 }

    val totalFreeMinutes = freeRanges.sumOf { it.duration }

    if (totalFreeMinutes <= 0) return emptyList()

    val totalPriority = subjects.sumOf { subject ->
        subject.priority.coerceAtLeast(1)
    }

    val allocations = subjects.map { subject ->
        val goal = goals.find { it.title == subject.name }
        if (goal != null) {
            if (!goal.increasePriorityOverTime) {
            SubjectAllocation(subject, 30)
        } else {
            val weight = calculateStageWeight(goal.startDate, goal.endDate, date)
            val adjustedPriority = (subject.priority * weight).toInt().coerceAtLeast(1)
            SubjectAllocation(subject, -1, adjustedPriority)
        }
        } else {
            SubjectAllocation(subject, -1, subject.priority)
        }
    }.toMutableList()

    val subjectNames = subjects.map { it.name }.toSet()
    val unmatchedGoals = goals.filter { it.title !in subjectNames }

    unmatchedGoals.forEach { goal ->
        // 과목은 없지만 목표가 있으므로, 목표 정보를 기반으로 가짜 과목 생성
        val dummySubject = SubjectItem(
        id = goal.id,        // 목표 ID를 사용
        name = goal.title,   // 목표 제목을 과목명으로 사용
        priority = 1,        // 기본 우선순위
        colorArgb = goalScheduleColorArgb(goal.id)
        )
        if (!goal.increasePriorityOverTime) {
            allocations.add(SubjectAllocation(dummySubject, 30))
        } else {
            val weight = calculateStageWeight(goal.startDate, goal.endDate, date)
            val adjustedPriority = (1 * weight).toInt().coerceAtLeast(1) // 기본 우선순위 1에 가중치 적용
            allocations.add(SubjectAllocation(dummySubject, -1, adjustedPriority))
        }
    }

    val fixedMinutes = allocations.filter { it.remainingMinutes > 0 }.sumOf { it.remainingMinutes }
    val remainingFreeMinutes = (totalFreeMinutes - fixedMinutes).coerceAtLeast(0)

    val weightSum = allocations.filter { it.remainingMinutes == -1 }.sumOf { it.priorityScore }
    allocations.filter { it.remainingMinutes == -1 }.forEach { alloc ->
        val portion = if (weightSum > 0) (remainingFreeMinutes * alloc.priorityScore / weightSum / 10) * 10 else 0
        alloc.remainingMinutes = portion
    }

    // 루프가 끝난 후, 최종 확정된 멤버들에게 시간 꽉 채워서 배정
    val finalFixedMins = allocations.filter { it.remainingMinutes > 0 }.sumOf { it.remainingMinutes }
    val finalFreeMins = (totalFreeMinutes - finalFixedMins).coerceAtLeast(0)
    val finalWeightSum = allocations.filter { it.remainingMinutes == -1 }.sumOf { it.priorityScore }
    allocations.filter { it.remainingMinutes == -1 }.forEach { alloc ->
        // 남은 시간을 우선순위에 따라 배분 (10분 단위 절삭)
        val portion = if (finalWeightSum > 0) (finalFreeMins * alloc.priorityScore / finalWeightSum / 10) * 10 else 0
        alloc.remainingMinutes = portion
    }

    allocations.sortByDescending { it.priorityScore.coerceAtLeast(it.subject.priority) }
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
                    // 남은 시간이 적으면 소진 처리하고 다음 과목으로
                    currentSubject.remainingMinutes = 0
                    allocationIndex++
                } else {
                    // 과목 시간은 많이 남았지만 현재 빈칸이 너무 좁은 경우 -> 다음 빈칸으로 이동
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
    return generatedSchedules
}

private fun goalScheduleColorArgb(seed: String): Int {
    val index = kotlin.math.abs(seed.hashCode()) % goalScheduleColorPalette.size
    return goalScheduleColorPalette[index]
}

private val goalScheduleColorPalette = listOf(
    0xFFBDE0FE.toInt(),
    0xFFD0E6FF.toInt(),
    0xFFBFCBFF.toInt(),
    0xFFD9C2F0.toInt(),
    0xFFEADCF8.toInt(),
    0xFFC7E9F1.toInt()
)
@RequiresApi(Build.VERSION_CODES.O)
private fun calculateStageWeight(startDate: String, endDate: String, today: LocalDate): Float {
    return try {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end).toFloat().coerceAtLeast(1f)
        val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(start, today).toFloat()

        val progress = elapsedDays / totalDays

        when {
            progress < 0.33f -> 1.0f // 1단계: 초기 (가중치 그대로)
            progress < 0.66f -> 1.5f // 2단계: 중기 (가중치 1.5배)
            else -> 2.0f             // 3단계: 말기 (가중치 2배)
        }
    } catch (e: Exception) {
        1.0f
    }
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
    val totalMinutes = minutes % (24 * 60)
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60

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
            TimeRange(wakeMinutes, 24 * 60),
            TimeRange(0, sleepMinutes)
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

