package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.coachHelpTarget
import com.example.studyapp.ui.settings.schedule.GoalViewModel
import com.example.studyapp.ui.settings.subject.SubjectViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    studiedMinutes: Int,
    commentOption: String,
    subjectViewModel: SubjectViewModel,
    isVisible: Boolean = true
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.DAILY) }

    val statsViewModel: StatsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val goalViewModel: GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(isVisible, studiedMinutes) {
        if (isVisible) {
            statsViewModel.loadStatsData()
            subjectViewModel.loadSubjectsFromFirestore()
            goalViewModel.loadGoalsFromFirestore()
        }
    }

    val records = remember(
        statsViewModel.records.toList(),
        statsViewModel.timerOverrideRecords.toList()
    ) {
        mergeStatsRecords(
            sessionRecords = statsViewModel.records,
            timerOverrideRecords = statsViewModel.timerOverrideRecords
        )
    }

    val currentSeconds = getCurrentPeriodStudySeconds(
        records = records,
        period = selectedPeriod
    )

    val previousSeconds = getPreviousPeriodStudySeconds(
        records = records,
        period = selectedPeriod
    )
    val cumulativeTitle = when (selectedPeriod) {
        StatsPeriod.DAILY -> "일별 누적 공부시간"
        StatsPeriod.WEEKLY -> "주별 누적 공부시간"
        StatsPeriod.MONTHLY -> "월별 누적 공부시간"
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
        val topPadding = 18.dp
        val bottomPadding = 12.dp
        val chartGap = 12.dp

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
            Text(
                text = "공부 통계",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.56f)
            ) {
                StatsFilterRow(
                    selected = selectedPeriod,
                    onSelect = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cumulativeTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatStatsDuration(currentSeconds),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = formatStatsDelta(currentSeconds - previousSeconds),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val chartTargetTopInset = 8.dp
                    val adjustedChartHeight = (maxHeight - chartTargetTopInset)
                        .coerceAtLeast(48.dp)

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = chartTargetTopInset),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .coachHelpTarget(CoachHelpTargets.StatsTotalChart)
                        )

                        StatsBarChart(
                            period = selectedPeriod,
                            records = records,
                            subjects = subjectViewModel.subjects,
                            goals = goalViewModel.goals,
                            chartHeight = adjustedChartHeight,
                            maxBarHeight = (adjustedChartHeight - 46.dp).coerceAtLeast(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(chartGap))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .coachHelpTarget(CoachHelpTargets.StatsFocusChart)
                    .weight(0.44f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier.padding(12.dp)
                ) {
                    StudyFocusLineChart(
                        points = hourlyFocusPoints,
                        chartHeight = (maxHeight - 48.dp).coerceAtLeast(48.dp)
                    )
                }
            }
        }
    }
}

private fun formatStatsDuration(seconds: Int): String {
    val totalMinutes = seconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분"
        hours > 0 -> "${hours}시간"
        else -> "${minutes}분"
    }
}

private fun formatStatsDelta(seconds: Int): String {
    val prefix = if (seconds >= 0) "+" else "-"
    return "$prefix${formatStatsDuration(kotlin.math.abs(seconds))}"
}

private fun mergeStatsRecords(
    sessionRecords: List<StudySessionRecord>,
    timerOverrideRecords: List<StudySessionRecord>
): List<StudySessionRecord> {
    val recordsByDateAndSubject = linkedMapOf<String, StudySessionRecord>()

    (sessionRecords + timerOverrideRecords)
        .groupBy { record -> "${record.sessionDate}|${record.subjectName}" }
        .forEach { (key, records) ->
            val sessionSeconds = records
                .filter { it.startTimeMillis > 0L || it.endTimeMillis > 0L }
                .sumOf { it.studiedSeconds }
            val overrideSeconds = records
                .filter { it.startTimeMillis == 0L && it.endTimeMillis == 0L }
                .sumOf { it.studiedSeconds }
            val baseRecord = records
                .firstOrNull { it.startTimeMillis > 0L || it.endTimeMillis > 0L }
                ?: records.first()
            val studiedSeconds = maxOf(sessionSeconds, overrideSeconds)

            if (studiedSeconds > 0) {
                recordsByDateAndSubject[key] = baseRecord.copy(
                    studiedSeconds = studiedSeconds
                )
            }
        }

    return recordsByDateAndSubject.values.toList()
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
