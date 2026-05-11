package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
fun generateHourlyFocusDataForLast30Days(
    records: List<StudySessionRecord>,
    startHour: Int,
    endHourExclusive: Int,
    zoneId: ZoneId = ZoneId.systemDefault()
): List<HourlyFocusPoint> {
    val now = Instant.now()
    val from = now.minus(30, ChronoUnit.DAYS)

    val hoursToShow = buildHourRange(
        startHour = startHour,
        endHourExclusive = endHourExclusive
    )

    val secondsByHour = hoursToShow.associateWith { 0 }.toMutableMap()

    records.forEach { record ->
        val recordStart = Instant.ofEpochMilli(record.startTimeMillis)
        val recordEnd = Instant.ofEpochMilli(record.endTimeMillis)

        // 최근 30일 범위 밖이면 제외
        if (!recordEnd.isAfter(from)) return@forEach
        if (!recordEnd.isAfter(recordStart)) return@forEach

        var segmentStart = if (recordStart.isBefore(from)) from else recordStart
        val segmentEnd = if (recordEnd.isAfter(now)) now else recordEnd

        while (segmentStart.isBefore(segmentEnd)) {
            val zonedStart = segmentStart.atZone(zoneId)

            val nextHour = zonedStart
                .truncatedTo(ChronoUnit.HOURS)
                .plusHours(1)
                .toInstant()

            val chunkEnd = if (nextHour.isBefore(segmentEnd)) {
                nextHour
            } else {
                segmentEnd
            }

            val seconds = ChronoUnit.SECONDS
                .between(segmentStart, chunkEnd)
                .toInt()
                .coerceAtLeast(0)

            val hour = zonedStart.hour

            // 사용자의 공부 가능 시간 범위 안에 있는 시간대만 누적
            if (secondsByHour.containsKey(hour)) {
                secondsByHour[hour] = (secondsByHour[hour] ?: 0) + seconds
            }

            segmentStart = chunkEnd
        }
    }

    val maxSeconds = max(secondsByHour.values.maxOrNull() ?: 0, 1)

    return hoursToShow.map { hour ->
        val seconds = secondsByHour[hour] ?: 0

        val studiedMinutes =
            if (seconds == 0) 0
            else (seconds + 59) / 60

        val focusScore =
            if (seconds == 0) {
                0
            } else {
                ((seconds.toDouble() / maxSeconds) * 100)
                    .roundToInt()
                    .coerceIn(0, 100)
            }

        HourlyFocusPoint(
            hour = hour,
            studiedMinutes = studiedMinutes,
            focusScore = focusScore
        )
    }
}

private fun buildHourRange(
    startHour: Int,
    endHourExclusive: Int
): List<Int> {
    val safeStartHour = startHour.coerceIn(0, 23)

    var safeEndHour = endHourExclusive.coerceIn(0, 24)

    // 예: 기상 07:00, 취침 00:00이면
    // 7 until 24 → 7시~23시까지 표시
    if (safeEndHour <= safeStartHour) {
        safeEndHour += 24
    }

    return (safeStartHour until safeEndHour).map { hour ->
        hour % 24
    }
}