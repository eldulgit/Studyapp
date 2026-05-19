package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayScheduleTimeline(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate?,
    schedules: List<DayScheduleBlock>,
    wakeTime: String,
    sleepTime: String
) {
    val actualDate = selectedDate ?: LocalDate.now()
    val displayedSchedules = schedules.filter { it.date == actualDate }

    val wakeMinutes = wakeTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
    val sleepMinutes = sleepTime.split(":").let {
        val mins = it[0].toInt() * 60 + it[1].toInt()
        if (mins < wakeMinutes) mins + 1440 else mins
    }

    val baseStartMinute = (wakeMinutes / 10) * 10
    val startHour = baseStartMinute / 60
    val endHour = (sleepMinutes + 59) / 60

    val rowHeight = 38.dp
    val timeLabelWidth = 56.dp
    val verticalScroll = rememberScrollState()

    val lineWidth = 0.5.dp
    val lineColor = Color(0xFFBDBDBD).copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val minuteCellWidth: Dp = (maxWidth - timeLabelWidth) / 6

            Column(
                modifier = Modifier
                    .fillMaxWidth().fillMaxHeight()
                    .verticalScroll(verticalScroll)
            ) {
                for (hour in startHour until endHour) {
                    val hourStartMinute = hour * 60
                    val hourEndMinute = hourStartMinute + 60
                    val rowSchedule = displayedSchedules.firstOrNull { schedule ->
                        val scheduleStart = schedule.startMinuteFrom(baseStartMinute)
                        val scheduleEnd = schedule.endMinuteFrom(baseStartMinute)

                        hourStartMinute < scheduleEnd && hourEndMinute > scheduleStart
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(timeLabelWidth)
                                .height(rowHeight)
                                .background(Color.White)
                                .border(lineWidth, lineColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = rowSchedule?.color?.copy(alpha = 0.72f)
                                            ?: Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatHourLabel(hour),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF111827)
                                )
                            }
                        }

                        for (slot in 0 until 6) {
                            val slotStartMinute = hour * 60 + slot * 10
                            val slotEndMinute = slotStartMinute + 10

                            val matched = displayedSchedules.find { schedule ->
                                val scheduleStart = schedule.startMinuteFrom(baseStartMinute)
                                val scheduleEnd = schedule.endMinuteFrom(baseStartMinute)

                                slotStartMinute < scheduleEnd && slotEndMinute > scheduleStart
                            }

                            Box(
                                modifier = Modifier
                                    .width(minuteCellWidth)
                                    .height(rowHeight)
                                    .background(matched?.color ?: Color.White)
                                    .border(lineWidth, lineColor),
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

private fun DayScheduleBlock.startMinuteFrom(baseStartMinute: Int): Int {
    return normalizeScheduleMinute(
        hour = startHour,
        minute = startMinute,
        baseStartMinute = baseStartMinute
    )
}

private fun DayScheduleBlock.endMinuteFrom(baseStartMinute: Int): Int {
    return normalizeScheduleMinute(
        hour = endHour,
        minute = endMinute,
        baseStartMinute = baseStartMinute
    )
}

private fun normalizeScheduleMinute(
    hour: Int,
    minute: Int,
    baseStartMinute: Int
): Int {
    val rawMinute = hour * 60 + minute

    return if (rawMinute < baseStartMinute) {
        rawMinute + 24 * 60
    } else {
        rawMinute
    }
}
private fun formatHourLabel(hour: Int): String {
    val normalized = when {
        hour < 24 -> hour
        else -> hour - 24
    }
    return normalized.toString().padStart(2, '0')
}
