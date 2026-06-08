package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.coachHelpTarget

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    studiedMinutes: Int,
    commentOption: String
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.DAILY) }

    val goalMinutes = when (selectedPeriod) {
        StatsPeriod.DAILY -> 120
        StatsPeriod.WEEKLY -> 600
        StatsPeriod.MONTHLY -> 2400
    }

    val goalSeconds = goalMinutes * 60

    val statsViewModel: StatsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        statsViewModel.loadStatsData()
    }

    val records = statsViewModel.records

    val currentSeconds = getCurrentPeriodStudySeconds(
        records = records,
        period = selectedPeriod
    )

    val previousSeconds = getPreviousPeriodStudySeconds(
        records = records,
        period = selectedPeriod
    )

    val commentTitle = when (commentOption) {
        "오늘의 명언" -> "오늘의 명언"
        "AI 코멘트" -> "AI 코치"
        else -> "AI 코치"
    }

    val comment = when (commentOption) {
        "오늘의 명언" -> getTodayQuote()

        "AI 코멘트" -> getAiStudyComment(
            currentSeconds = currentSeconds,
            previousSeconds = previousSeconds,
            goalSeconds = goalSeconds,
            period = selectedPeriod
        )

        else -> getAiStudyComment(
            currentSeconds = currentSeconds,
            previousSeconds = previousSeconds,
            goalSeconds = goalSeconds,
            period = selectedPeriod
        )
    }

    val userProfile = statsViewModel.userProfile
    val recordSnapshot = records.toList()

    val studyHourRange = remember(userProfile) {
        getStudyHourRangeFromUserProfile(
            wakeTime = userProfile?.wakeTime,
            sleepTime = userProfile?.sleepTime
        )
    }

    val hourlyFocusPoints = remember(recordSnapshot, studyHourRange) {
        generateHourlyFocusDataForLast30Days(
            records = recordSnapshot,
            startHour = studyHourRange.startHour,
            endHourExclusive = studyHourRange.endHourExclusive
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val horizontalPadding = 16.dp
        val topPadding = 8.dp
        val bottomPadding = 8.dp
        val chartGap = 12.dp
        val commentGap = 2.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = topPadding,
                    bottom = bottomPadding
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            ) {
                StatsFilterRow(
                    selected = selectedPeriod,
                    onSelect = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(4.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val chartTargetTopInset = 8.dp
                    val adjustedChartHeight = (maxHeight - chartTargetTopInset)
                        .coerceAtLeast(48.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = chartTargetTopInset)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .coachHelpTarget(CoachHelpTargets.StatsTotalChart)
                        )

                    StatsBarChart(
                        period = selectedPeriod,
                        records = records,
                            chartHeight = adjustedChartHeight,
                            maxBarHeight = (adjustedChartHeight - 46.dp).coerceAtLeast(24.dp)
                    )
                    }
                }
            }

            Spacer(modifier = Modifier.height(chartGap))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .coachHelpTarget(CoachHelpTargets.StatsFocusChart)
                    .weight(0.45f)
            ) {
                StudyFocusLineChart(
                    points = hourlyFocusPoints,
                    chartHeight = (maxHeight - 48.dp).coerceAtLeast(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(commentGap))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                contentAlignment = Alignment.CenterStart
            ) {
                StatsCommentSection(
                    title = commentTitle,
                    comment = comment
                )
            }
        }
    }
}

private data class StudyHourRange(
    val startHour: Int,
    val endHourExclusive: Int
)

private fun getStudyHourRangeFromUserProfile(
    wakeTime: String?,
    sleepTime: String?
): StudyHourRange {
    val wakeMinutes = parseTimeToMinutes(wakeTime) ?: (7 * 60)
    val sleepMinutes = parseTimeToMinutes(sleepTime) ?: (23 * 60)

    val startHour = wakeMinutes / 60

    // 23:30이면 23시대까지 보여줘야 하므로 endHourExclusive는 24
    // 23:00이면 23시대는 포함하지 않고 22시대까지만 보여줌
    val endHourExclusive = (sleepMinutes + 59) / 60

    return StudyHourRange(
        startHour = startHour.coerceIn(0, 23),
        endHourExclusive = endHourExclusive.coerceIn(0, 24)
    )
}

private fun parseTimeToMinutes(time: String?): Int? {
    if (time.isNullOrBlank()) return null

    val parts = time.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return hour * 60 + minute
}
