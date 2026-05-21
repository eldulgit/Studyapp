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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.theme.isAppInDarkTheme
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
    val isDarkTheme = isAppInDarkTheme()
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.White
    }
    val lineColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    } else {
        Color(0xFFEAEAEA)
    }

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
                    color = lineColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(containerColor)
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
                        .border(0.5.dp, lineColor)
                )

                days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .height(headerHeight)
                            .border(0.5.dp, lineColor),
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
                    width = timeColumnWidth,
                    lineColor = lineColor
                )

                days.forEach { day ->
                    DayColumn(
                        day = day,
                        items = scheduleItems,
                        startHour = startHour,
                        totalHours = totalHours,
                        hourHeight = hourHeight,
                        width = dayColumnWidth,
                        lineColor = lineColor,
                        isDarkTheme = isDarkTheme,
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
    width: Dp,
    lineColor: Color
) {
    Column(
        modifier = Modifier.width(width)
    ) {
        repeat(totalHours) { index ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(hourHeight)
                    .border(0.5.dp, lineColor),
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
    lineColor: Color,
    isDarkTheme: Boolean,
    onItemClick: (FixedScheduleItem) -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(hourHeight * totalHours)
            .border(0.5.dp, lineColor)
    ) {
        Column(
            modifier = Modifier.matchParentSize()
        ) {
            repeat(totalHours) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
                        .border(0.5.dp, lineColor)
                )
            }
        }
        items
            .filter {
                it.dayOfWeek == day &&
                        it.startTime != null &&
                        it.endTime != null
            }
            .forEach { item ->
                val startMinutes = parseTimeToMinutes(item.startTime)
                val endMinutes = parseTimeToMinutes(item.endTime)

                if (startMinutes != null && endMinutes != null && endMinutes > startMinutes) {
                    val baseMinutes = startHour * 60
                    val topOffset = ((startMinutes - baseMinutes) / 60f) * hourHeight.value
                    val blockHeight = ((endMinutes - startMinutes) / 60f) * hourHeight.value

                    val color = timetableColors[
                        item.stableColorIndex(timetableColors.size)
                    ].forTheme(isDarkTheme)
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(blockHeight.dp.coerceAtLeast(1.dp))
                            .offset(y = topOffset.dp)
                            .background(color.container)
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = color.text
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
    val text: Color
)

private fun TimetableColorSet.forTheme(darkTheme: Boolean): TimetableColorSet {
    if (!darkTheme) return this

    return TimetableColorSet(
        container = lerp(container, Color(0xFF334155), 0.42f),
        text = lerp(text, Color(0xFFE2E8F0), 0.68f)
    )
}

private fun FixedScheduleItem.stableColorIndex(colorCount: Int): Int {
    val seed = firestoreId ?: "$id-$title-$dayOfWeek-$startTime-$endTime"
    return kotlin.math.abs(seed.hashCode()) % colorCount
}

private val timetableColors = listOf(
    TimetableColorSet(
        container = Color(0xFFFBE2E2),
        text = Color(0xFF6A3D3D)
    ),
    TimetableColorSet(
        container = Color(0xFFFBEAD5),
        text = Color(0xFF6A4B2C)
    ),
    TimetableColorSet(
        container = Color(0xFFF8F0C9),
        text = Color(0xFF665C2C)
    ),
    TimetableColorSet(
        container = Color(0xFFE4F2DB),
        text = Color(0xFF3F6137)
    ),
    TimetableColorSet(
        container = Color(0xFFDDF2EC),
        text = Color(0xFF315E54)
    ),
    TimetableColorSet(
        container = Color(0xFFE2EBFA),
        text = Color(0xFF354B68)
    ),
    TimetableColorSet(
        container = Color(0xFFF0E3F8),
        text = Color(0xFF543D67)
    )
)
