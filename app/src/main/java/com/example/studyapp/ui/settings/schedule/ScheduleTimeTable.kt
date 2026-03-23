package com.example.studyapp.ui.settings.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ScheduleTimetable(
    items: List<FixedScheduleItem>,
    modifier: Modifier = Modifier,
    onItemClick: (FixedScheduleItem) -> Unit = {}
) {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")

    val scheduleItems = items.filter {
        it.category == ScheduleCategory.SCHEDULE &&
                it.dayOfWeek != null &&
                it.startTime != null &&
                it.endTime != null
    }

    // 기본 범위는 9~17
    val defaultStartHour = 9
    val defaultEndHour = 17

    val earliestHour = scheduleItems
        .mapNotNull { parseTimeToMinutes(it.startTime) }
        .minOrNull()
        ?.let { floor(it / 60f).toInt() }
        ?: defaultStartHour

    val latestHour = scheduleItems
        .mapNotNull { parseTimeToMinutes(it.endTime) }
        .maxOrNull()
        ?.let { ceil(it / 60f).toInt() }
        ?: defaultEndHour

    val startHour = minOf(defaultStartHour, earliestHour)
    val endHour = maxOf(defaultEndHour, latestHour)

    val totalHours = (endHour - startHour).coerceAtLeast(1)

    // 세로 간격 조금 줄임
    val hourHeight = 56.dp
    val headerHeight = 34.dp
    val timeColumnWidth = 34.dp
    val timetableHeight = hourHeight * totalHours

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val dayColumnWidth = (maxWidth - timeColumnWidth) / days.size

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Box(
                    modifier = Modifier
                        .width(timeColumnWidth)
                        .height(headerHeight)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                )

                days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .height(headerHeight)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(timetableHeight)
            ) {
                TimeColumn(
                    startHour = startHour,
                    totalHours = totalHours,
                    hourHeight = hourHeight,
                    width = timeColumnWidth
                )

                days.forEach { day ->
                    DayColumn(
                        day = day,
                        items = scheduleItems,
                        startHour = startHour,
                        totalHours = totalHours,
                        hourHeight = hourHeight,
                        width = dayColumnWidth,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    startHour: Int,
    totalHours: Int,
    hourHeight: Dp,
    width: Dp
) {
    Column(
        modifier = Modifier.width(width)
    ) {
        repeat(totalHours) { index ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(hourHeight)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = String.format("%02d", startHour + index),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: String,
    items: List<FixedScheduleItem>,
    startHour: Int,
    totalHours: Int,
    hourHeight: Dp,
    width: Dp,
    onItemClick: (FixedScheduleItem) -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(hourHeight * totalHours)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.matchParentSize()
        ) {
            repeat(totalHours) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        items
            .filter {
                it.dayOfWeek == day &&
                        it.startTime != null &&
                        it.endTime != null
            }
            .forEachIndexed { index, item ->
                val startMinutes = parseTimeToMinutes(item.startTime)
                val endMinutes = parseTimeToMinutes(item.endTime)

                if (startMinutes != null && endMinutes != null && endMinutes > startMinutes) {
                    val baseMinutes = startHour * 60
                    val topOffset = ((startMinutes - baseMinutes) / 60f) * hourHeight.value
                    val blockHeight = ((endMinutes - startMinutes) / 60f) * hourHeight.value

                    val color = timetableColors[index % timetableColors.size]

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                            .width(width - 4.dp)
                            .height((blockHeight.dp - 2.dp).coerceAtLeast(24.dp))
                            .offset(y = topOffset.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.container)
                            .border(
                                width = 1.dp,
                                color = color.border,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = color.text,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
    }
}

private fun parseTimeToMinutes(time: String?): Int? {
    if (time.isNullOrBlank()) return null
    val parts = time.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}

private data class TimetableColorSet(
    val container: Color,
    val border: Color,
    val text: Color
)

private val timetableColors = listOf(
    TimetableColorSet(
        container = Color(0xFFE8B6C1),
        border = Color(0xFFD59AA8),
        text = Color(0xFF5C3A42)
    ),
    TimetableColorSet(
        container = Color(0xFFF1C8A8),
        border = Color(0xFFE0B18C),
        text = Color(0xFF6A4936)
    ),
    TimetableColorSet(
        container = Color(0xFFE89B93),
        border = Color(0xFFD9857C),
        text = Color(0xFF633733)
    ),
    TimetableColorSet(
        container = Color(0xFFD99595),
        border = Color(0xFFC97D7D),
        text = Color(0xFF5A3131)
    ),
    TimetableColorSet(
        container = Color(0xFFE6AAA0),
        border = Color(0xFFD69286),
        text = Color(0xFF613B34)
    ),
    TimetableColorSet(
        container = Color(0xFFCFC7F6),
        border = Color(0xFFB9AEEF),
        text = Color(0xFF433C69)
    ),
    TimetableColorSet(
        container = Color(0xFFBFE3DC),
        border = Color(0xFF9FD0C6),
        text = Color(0xFF2F5B53)
    )
)