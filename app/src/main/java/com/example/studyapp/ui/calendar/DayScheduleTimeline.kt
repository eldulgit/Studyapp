package com.example.studyapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DayScheduleTimeline(
    selectedDate: LocalDate?,
    schedules: List<DayScheduleBlock>
) {
    val actualDate = selectedDate ?: LocalDate.now()
    val displayedSchedules = schedules.filter { it.date == actualDate }

    val minHourFromSchedule = displayedSchedules.minOfOrNull { it.startHour }
    val maxHourFromSchedule = displayedSchedules.maxOfOrNull {
        if (it.endMinute > 0) it.endHour + 1 else it.endHour
    }

    val startHour = minOf(17, minHourFromSchedule ?: 17)
    val endHour = maxOf(24, maxHourFromSchedule ?: 24)

    val rowHeight = 38.dp
    val timeLabelWidth = 56.dp
    val verticalScroll = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Scheduler",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val minuteCellWidth: Dp = (maxWidth - timeLabelWidth) / 6

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                .border(1.dp, Color(0xFF8E8E8E)),
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
                                val scheduleStart = schedule.startHour * 60 + schedule.startMinute
                                val scheduleEnd = schedule.endHour * 60 + schedule.endMinute
                                slotStartMinute < scheduleEnd && slotEndMinute > scheduleStart
                            }

                            Box(
                                modifier = Modifier
                                    .width(minuteCellWidth)
                                    .height(rowHeight)
                                    .border(1.dp, Color(0xFF8E8E8E))
                                    .background(matched?.color ?: Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (matched != null) {
                                    val isStartSlot =
                                        slotStartMinute == (matched.startHour * 60 + matched.startMinute)

                                    if (isStartSlot) {
                                        Text(
                                            text = matched.subject,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHourLabel(hour: Int): String {
    val normalized = when {
        hour < 24 -> hour
        else -> hour - 24
    }
    return normalized.toString().padStart(2, '0')
}