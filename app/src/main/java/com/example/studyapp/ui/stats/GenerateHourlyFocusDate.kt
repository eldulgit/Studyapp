package com.example.studyapp.ui.stats

import com.example.studyapp.ai.DailyScheduleItem
import kotlin.math.roundToInt

fun generateHourlyFocusData(
    schedules: List<DailyScheduleItem>,
    wakeUpTime: String,
    sleepTime: String
): List<HourlyFocusPoint> {
    val wake = wakeUpTime.toMinutesOrNull() ?: return emptyList()
    val sleep = sleepTime.toMinutesOrNull() ?: return emptyList()

    if (wake >= sleep) return emptyList()

    val points = mutableListOf<HourlyFocusPoint>()

    var slotStart = wake

    while (slotStart < sleep) {
        val slotEnd = minOf(slotStart + 60, sleep)

        val focusScore = calculateFocusScoreForSlot(
            slotStart = slotStart,
            slotEnd = slotEnd,
            schedules = schedules
        )

        points.add(
            HourlyFocusPoint(
                hour = slotStart / 60,
                studiedMinutes = 0,
                focusScore = focusScore
            )
        )

        slotStart += 60
    }

    return points
}

private fun calculateFocusScoreForSlot(
    slotStart: Int,
    slotEnd: Int,
    schedules: List<DailyScheduleItem>
): Int {
    val slotLength = slotEnd - slotStart
    if (slotLength <= 0) return 0

    var totalWeightedMinutes = 0.0

    schedules.forEach { schedule ->
        val scheduleStart = schedule.startTime.toMinutesOrNull() ?: return@forEach
        val scheduleEnd = schedule.endTime.toMinutesOrNull() ?: return@forEach

        val overlapStart = maxOf(slotStart, scheduleStart)
        val overlapEnd = minOf(slotEnd, scheduleEnd)
        val overlapMinutes = overlapEnd - overlapStart

        if (overlapMinutes > 0) {
            val priorityWeight = when (schedule.priority) {
                3 -> 1.0
                2 -> 0.75
                else -> 0.5
            }

            totalWeightedMinutes += overlapMinutes * priorityWeight
        }
    }

    return ((totalWeightedMinutes / slotLength) * 100)
        .roundToInt()
        .coerceIn(0, 100)
}

private fun String.toMinutesOrNull(): Int? {
    val parts = this.split(":")
    if (parts.size < 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return hour * 60 + minute
}