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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme
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
    val isDarkTheme = isAppInDarkTheme()

    val wakeMinutes = wakeTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
    val sleepMinutes = sleepTime.split(":").let {
        val mins = it[0].toInt() * 60 + it[1].toInt()
        if (mins < wakeMinutes) mins + 1440 else mins
    }

    val baseStartMinute = (wakeMinutes / 10) * 10
    val startHour = baseStartMinute / 60
    val endHour = (sleepMinutes + 59) / 60

    val timeLabelWidth = 46.dp

    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val hourTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp)
        ) {
            val contentWidth: Dp = maxWidth - timeLabelWidth

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                for (hour in startHour until endHour) {
                    val hourStartMinute = hour * 60

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(timeLabelWidth)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatHourLabel(hour),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = hourTextColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(contentWidth)
                                .fillMaxHeight()
                                .border(
                                    width = 0.5.dp,
                                    color = lineColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(3.dp)
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val slotWidth = maxWidth / 6

                                Row(modifier = Modifier.fillMaxSize()) {
                                    repeat(6) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .border(
                                                    width = 0.5.dp,
                                                    color = lineColor.copy(alpha = 0.28f)
                                                )
                                        )
                                    }
                                }

                                displayedSchedules.forEach { schedule ->
                                    val hourEndMinute = hourStartMinute + 60
                                    val scheduleStart = schedule.startMinuteFrom(baseStartMinute)
                                    val scheduleEnd = schedule.endMinuteFrom(baseStartMinute)

                                    if (hourStartMinute < scheduleEnd && hourEndMinute > scheduleStart) {
                                        val segmentStart = maxOf(scheduleStart, hourStartMinute)
                                        val segmentEnd = minOf(scheduleEnd, hourEndMinute)
                                        val startSlot = ((segmentStart - hourStartMinute) / 10)
                                            .coerceIn(0, 6)
                                        val endSlot = ((segmentEnd - hourStartMinute + 9) / 10)
                                            .coerceIn(startSlot, 6)
                                        val slotCount = (endSlot - startSlot).coerceAtLeast(1)
                                        val baseColor = subjectColorForTheme(schedule.color, isDarkTheme)
                                        val chipColor = lerp(
                                            baseColor,
                                            MaterialTheme.colorScheme.surface,
                                            0.28f
                                        )
                                        val textColor = if (isDarkTheme) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            lerp(baseColor, Color(0xFF0F172A), 0.64f)
                                        }
                                        val shouldShowLabel =
                                            scheduleStart >= hourStartMinute &&
                                                    scheduleStart < hourEndMinute ||
                                                    scheduleStart < hourStartMinute &&
                                                    scheduleEnd > hourStartMinute

                                        Box(
                                            modifier = Modifier
                                                .offset(x = slotWidth * startSlot)
                                                .width(slotWidth * slotCount)
                                                .fillMaxHeight()
                                                .padding(end = 6.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(chipColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (shouldShowLabel) {
                                                Text(
                                                    text = schedule.subject,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = textColor
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
