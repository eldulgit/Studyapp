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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayScheduleTimeline(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate?,
    schedules: List<DayScheduleBlock>
) {
    val actualDate = selectedDate ?: LocalDate.now()
    val displayedSchedules = schedules.filter { it.date == actualDate }

    val startHour = 12
    val endHour = 26

    val rowHeight = 38.dp
    val timeLabelWidth = 56.dp
    val verticalScroll = rememberScrollState()

    val lineWidth = 0.5.dp
    val lineColor = Color(0xFFBDBDBD).copy(alpha = 0.6f)

    Column(
        modifier = modifier.fillMaxWidth()
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
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(timeLabelWidth)
                                .height(rowHeight)
                                .border(lineWidth, lineColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatHourLabel(hour),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8A8A8A)
                            )
                        }

                        for (slot in 0 until 6) {
                            val slotStartMinute = hour * 60 + slot * 10
                            val slotEndMinute = slotStartMinute + 10

                            val matched = displayedSchedules.find { schedule ->
                                val scheduleStart = normalizeScheduleMinute(
                                    schedule.startHour,
                                    schedule.startMinute
                                )
                                val scheduleEnd = normalizeScheduleMinute(
                                    schedule.endHour,
                                    schedule.endMinute
                                )

                                slotStartMinute < scheduleEnd && slotEndMinute > scheduleStart
                            }

                            Box(
                                modifier = Modifier
                                    .width(minuteCellWidth)
                                    .height(rowHeight)
                                    .border(lineWidth, lineColor)
                                    .background(matched?.color ?: Color.Transparent),
                                contentAlignment = Alignment.Center
                            ){

                            }
                        }
                    }
                }
            }
        }
    }
}

private fun normalizeScheduleMinute(hour: Int, minute: Int): Int {
    val normalizedHour = if (hour < 12) hour + 24 else hour
    return normalizedHour * 60 + minute
}

private fun formatHourLabel(hour: Int): String {
    val normalized = when {
        hour < 24 -> hour
        else -> hour - 24
    }
    return normalized.toString().padStart(2, '0')
}