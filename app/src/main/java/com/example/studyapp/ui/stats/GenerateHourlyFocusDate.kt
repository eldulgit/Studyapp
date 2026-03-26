package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.O)
fun generateHourlyFocusData(
    records: List<StudySessionRecord>,
    scheduledHours: List<Int>,
    days: Long = 30L,
    zoneId: ZoneId = ZoneId.systemDefault()
): List<HourlyFocusPoint> {

    val now = Instant.now()
    val from = now.minus(days, ChronoUnit.DAYS)

    val secondsByHour = mutableMapOf<Int, Int>().apply {
        scheduledHours.distinct().sorted().forEach { hour ->
            this[hour] = 0
        }
    }

    records.forEach { record ->
        val recordStart = Instant.ofEpochMilli(record.startTimeMillis)
        val recordEnd = Instant.ofEpochMilli(record.endTimeMillis)

        if (recordEnd.isBefore(from) || !recordEnd.isAfter(recordStart)) {
            return@forEach
        }

        var segmentStart = if (recordStart.isBefore(from)) from else recordStart
        val segmentEnd = if (recordEnd.isAfter(now)) now else recordEnd

        while (segmentStart.isBefore(segmentEnd)) {
            val zoned = segmentStart.atZone(zoneId)
            val nextHour = zoned.truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant()
            val chunkEnd = if (nextHour.isBefore(segmentEnd)) nextHour else segmentEnd

            val seconds = ChronoUnit.SECONDS.between(segmentStart, chunkEnd).toInt()
            val hour = zoned.hour

            if (secondsByHour.containsKey(hour)) {
                secondsByHour[hour] = (secondsByHour[hour] ?: 0) + max(seconds, 0)
            }

            segmentStart = chunkEnd
        }
    }

    return scheduledHours
        .distinct()
        .sorted()
        .map { hour ->
            HourlyFocusPoint(
                hour = hour,
                studiedMinutes = (secondsByHour[hour] ?: 0) / 60
            )
        }
}