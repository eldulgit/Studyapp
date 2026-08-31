package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.calendar.goalScheduleColorArgb
import com.example.studyapp.ui.settings.schedule.GoalItem
import com.example.studyapp.ui.settings.subject.SubjectItem
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme
import com.example.studyapp.util.AppTimeZone
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsBarChart(
    period: StatsPeriod,
    records: List<StudySessionRecord>,
    subjects: List<SubjectItem>,
    goals: List<GoalItem>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 150.dp,
    maxBarHeight: Dp = 104.dp
) {
    val labels = generateLabels(period)

    val barSegments = generateBarSegments(
        records = records,
        period = period,
        labelCount = labels.size
    )
    val studiedSecondsValues = barSegments.map { segments ->
        segments.sumOf { it.studiedSeconds }
    }
    val chartValues = studiedSecondsValues.map { it.toChartValue() }
    val subjectColorMap = subjects.associate { subject ->
        subject.name.trim() to Color(subject.colorArgb)
    } + goals.associate { goal ->
        goal.title.trim() to Color(goalScheduleColorArgb(goal.id))
    }
    val isDarkTheme = isAppInDarkTheme()

    val hasStudyData = studiedSecondsValues.any { it > 0 }
    val yAxisValues = buildYAxisValues(
        maxChartValue = chartValues.maxOrNull() ?: 0f,
        hasStudyData = hasStudyData,
        period = period
    )
    val scaleMaxValue = yAxisValues.first().coerceAtLeast(1f)
    val yAxisWidth = 30.dp
    val plotStartPadding = 2.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(start = 4.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(maxBarHeight),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yAxisValues.forEach { value ->
                    Text(
                        text = formatAxisDurationLabel(value, period),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(maxBarHeight)
                    .padding(start = plotStartPadding)
            ) {
                Column(
                    modifier = Modifier.matchParentSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yAxisValues.forEach {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }

                Row(
                    modifier = Modifier.matchParentSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barSegments.forEachIndexed { index, segments ->
                        val totalValue = chartValues[index]
                        val totalRatio = (totalValue / scaleMaxValue).coerceIn(0f, 1f)
                        val totalBarHeight = (totalRatio * maxBarHeight.value).dp
                        val totalSeconds = studiedSecondsValues[index]

                        Column(
                            modifier = Modifier
                                .width(24.dp)
                                .height(totalBarHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            segments.forEach { segment ->
                                val segmentRatio = if (totalSeconds > 0) {
                                    segment.studiedSeconds.toFloat() / totalSeconds
                                } else {
                                    0f
                                }
                                val segmentHeight = (totalBarHeight.value * segmentRatio).dp
                                val subjectColor = subjectColorMap[segment.subjectName.trim()]
                                    ?.let { subjectColorForTheme(it, isDarkTheme) }
                                    ?: MaterialTheme.colorScheme.primary

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(segmentHeight)
                                        .background(subjectColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(yAxisWidth + plotStartPadding))
            Divider(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.width(yAxisWidth + plotStartPadding))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateBarSegments(
    records: List<StudySessionRecord>,
    period: StatsPeriod,
    labelCount: Int
): List<List<SubjectStudySegment>> {
    val today = LocalDate.now(AppTimeZone.zoneId)

    return when (period) {
        StatsPeriod.DAILY -> {
            val startDate = today.minusDays((labelCount - 1).toLong())

            List(labelCount) { index ->
                val date = startDate.plusDays(index.toLong()).toString()

                records
                    .filter { it.sessionDate == date }
                    .toSubjectStudySegments()
            }
        }

        StatsPeriod.WEEKLY -> {
            val currentWeekStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
            )

            val startWeek = currentWeekStart.minusWeeks((labelCount - 1).toLong())

            List(labelCount) { index ->
                val weekStart = startWeek.plusWeeks(index.toLong())
                val weekEnd = weekStart.plusDays(6)

                records
                    .filter { record ->
                        val recordDate = LocalDate.parse(record.sessionDate)

                        !recordDate.isBefore(weekStart) &&
                                !recordDate.isAfter(weekEnd)
                    }
                    .toSubjectStudySegments()
            }
        }

        StatsPeriod.MONTHLY -> {
            val startMonth = today
                .minusMonths((labelCount - 1).toLong())
                .withDayOfMonth(1)

            List(labelCount) { index ->
                val targetMonth = startMonth.plusMonths(index.toLong())

                records
                    .filter { record ->
                        val recordDate = LocalDate.parse(record.sessionDate)

                        recordDate.year == targetMonth.year &&
                                recordDate.monthValue == targetMonth.monthValue
                    }
                    .toSubjectStudySegments()
            }
        }
    }
}

private data class SubjectStudySegment(
    val subjectName: String,
    val studiedSeconds: Int
)

private fun List<StudySessionRecord>.toSubjectStudySegments(): List<SubjectStudySegment> {
    return groupBy { it.subjectName.trim() }
        .map { (subjectName, records) ->
            SubjectStudySegment(
                subjectName = subjectName,
                studiedSeconds = records.sumOf { it.studiedSeconds }
            )
        }
        .filter { it.studiedSeconds > 0 }
        .sortedByDescending { it.studiedSeconds }
}

private fun Int.toChartValue(): Float {
    return this.toFloat()
}

private fun buildYAxisValues(
    maxChartValue: Float,
    hasStudyData: Boolean,
    period: StatsPeriod
): List<Float> {
    if (period == StatsPeriod.MONTHLY) {
        val topHour = if (!hasStudyData) {
            3
        } else {
            ceil(maxChartValue / 1.hours).toInt().coerceAtLeast(3)
        }

        return listOf(
            topHour.hours,
            ((topHour * 2f) / 3f).hours,
            (topHour / 3f).hours,
            0f
        )
    }

    if (!hasStudyData) {
        return listOf(20.minutes, 10.minutes, 1.minutes, 0f)
    }

    val topValue = when {
        maxChartValue <= 1.minutes -> 1.minutes
        maxChartValue <= 10.minutes -> 10.minutes
        maxChartValue <= 20.minutes -> 20.minutes
        maxChartValue <= 1.hours -> 1.hours
        else -> ceil(maxChartValue / 1.hours) * 1.hours
    }

    return when (topValue) {
        1.minutes -> listOf(1.minutes, 40f, 20f, 0f)
        10.minutes -> listOf(10.minutes, 5.minutes, 1.minutes, 0f)
        20.minutes -> listOf(20.minutes, 10.minutes, 1.minutes, 0f)
        1.hours -> listOf(1.hours, 30.minutes, 10.minutes, 0f)
        else -> listOf(topValue, topValue / 2f, topValue / 4f, 0f)
    }
}

private fun formatAxisDurationLabel(chartValue: Float, period: StatsPeriod): String {
    if (chartValue <= 0f) return "0"

    val totalSeconds = chartValue.toInt().coerceAtLeast(1)
    if (period == StatsPeriod.MONTHLY) {
        val hours = ceil(totalSeconds / 3600f).toInt().coerceAtLeast(1)
        return "${hours}h"
    }

    if (totalSeconds < 60) return "${totalSeconds}S"

    val totalMinutes = totalSeconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}H${minutes}M"
        hours > 0 -> "${hours}H"
        else -> "${minutes}M"
    }
}

private val Int.minutes: Float
    get() = this * 60f

private val Int.hours: Float
    get() = this * 60.minutes

private val Float.hours: Float
    get() = this * 60.minutes
