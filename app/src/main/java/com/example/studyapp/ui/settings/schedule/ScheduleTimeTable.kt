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
    val scheduleItems = items.filter {
        it.category == ScheduleCategory.SCHEDULE &&
                it.dayOfWeek != null &&
                it.startTime != null &&
                it.endTime != null
    }
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    val colorIndexByTitle = buildScheduleColorIndexMap(
        items = scheduleItems,
        days = days,
        colorCount = timetableColors.size
    )

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
    val hourHeight = 52.dp
    val headerHeight = 32.dp
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
        Color(0xFFE2E8F0).copy(alpha = 0.62f)
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
                    color = lineColor.copy(alpha = 0.7f),
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
                        .border(0.5.dp, lineColor.copy(alpha = 0.35f))
                )

                days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .height(headerHeight)
                            .border(0.5.dp, lineColor.copy(alpha = 0.35f)),
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
                    lineColor = lineColor,
                    isDarkTheme = isDarkTheme
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
                        colorIndexByTitle = colorIndexByTitle,
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
    lineColor: Color,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier.width(width)
    ) {
        repeat(totalHours) { index ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(hourHeight)
                    .border(0.5.dp, lineColor.copy(alpha = 0.35f)),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = String.format("%02d", startHour + index),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    colorIndexByTitle: Map<String, Int>,
    onItemClick: (FixedScheduleItem) -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(hourHeight * totalHours)
            .border(0.5.dp, lineColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.matchParentSize()
        ) {
            repeat(totalHours) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
                        .border(0.5.dp, lineColor.copy(alpha = 0.28f))
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
                        colorIndexByTitle[item.normalizedTitle()]
                            ?: item.stableColorIndex(timetableColors.size)
                    ].forTheme(isDarkTheme)
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(blockHeight.dp.coerceAtLeast(1.dp))
                            .offset(y = topOffset.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.container)
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall,
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
        container = lerp(container, Color(0xFF334155), 0.28f),
        text = lerp(text, Color(0xFFE2E8F0), 0.52f)
    )
}

private fun FixedScheduleItem.stableColorIndex(colorCount: Int): Int {
    return floorMod(normalizedTitle().hashCode(), colorCount)
}

private fun FixedScheduleItem.normalizedTitle(): String {
    return title.trim().ifBlank { firestoreId ?: id.toString() }
}

private fun buildScheduleColorIndexMap(
    items: List<FixedScheduleItem>,
    days: List<String>,
    colorCount: Int
): Map<String, Int> {
    if (colorCount <= 0) return emptyMap()

    val titles = items
        .map { it.normalizedTitle() }
        .distinct()
        .sorted()
    val adjacentTitles = titles.associateWith { mutableSetOf<String>() }

    items.forEachIndexed { index, item ->
        val itemTitle = item.normalizedTitle()
        val itemStart = parseTimeToMinutes(item.startTime) ?: return@forEachIndexed
        val itemEnd = parseTimeToMinutes(item.endTime) ?: return@forEachIndexed
        val itemDayIndex = days.indexOf(item.dayOfWeek)

        items.drop(index + 1).forEach { other ->
            val otherTitle = other.normalizedTitle()
            if (itemTitle == otherTitle) return@forEach
            val otherDayIndex = days.indexOf(other.dayOfWeek)
            if (itemDayIndex == -1 || otherDayIndex == -1) return@forEach
            if (kotlin.math.abs(itemDayIndex - otherDayIndex) > 1) return@forEach

            val otherStart = parseTimeToMinutes(other.startTime) ?: return@forEach
            val otherEnd = parseTimeToMinutes(other.endTime) ?: return@forEach

            if (itemStart <= otherEnd && otherStart <= itemEnd) {
                adjacentTitles[itemTitle]?.add(otherTitle)
                adjacentTitles[otherTitle]?.add(itemTitle)
            }
        }
    }

    val assigned = mutableMapOf<String, Int>()

    titles
        .sortedWith(
            compareByDescending<String> { adjacentTitles[it]?.size ?: 0 }
                .thenBy { it }
        )
        .forEach { title ->
            val blockedColors = adjacentTitles[title]
                .orEmpty()
                .mapNotNull { assigned[it] }
                .toSet()
            val preferredColor = floorMod(title.hashCode(), colorCount)
            val availableColor = (0 until colorCount)
                .map { (preferredColor + it) % colorCount }
                .firstOrNull { it !in blockedColors }
                ?: preferredColor

            assigned[title] = availableColor
        }

    return assigned
}

private fun floorMod(value: Int, modulus: Int): Int {
    return ((value % modulus) + modulus) % modulus
}

private val timetableColors = listOf(
    TimetableColorSet(
        container = Color(0xFFFFE4E6),
        text = Color(0xFF9F1239)
    ),
    TimetableColorSet(
        container = Color(0xFFFEF3C7),
        text = Color(0xFF92400E)
    ),
    TimetableColorSet(
        container = Color(0xFFF3E8FF),
        text = Color(0xFF6B21A8)
    ),
    TimetableColorSet(
        container = Color(0xFFECFDF5),
        text = Color(0xFF047857)
    ),
    TimetableColorSet(
        container = Color(0xFFE0F2FE),
        text = Color(0xFF0369A1)
    ),
    TimetableColorSet(
        container = Color(0xFFEEF2FF),
        text = Color(0xFF3730A3)
    ),
    TimetableColorSet(
        container = Color(0xFFEFF6FF),
        text = Color(0xFF1D4ED8)
    )
)
