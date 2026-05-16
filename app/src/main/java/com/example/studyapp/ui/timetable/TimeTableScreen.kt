package com.example.studyapp.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.schedule.FixedScheduleItem
import com.example.studyapp.ui.settings.schedule.ScheduleCategory
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun TimeTableScreen(
    subjectViewModel: SubjectViewModel,
    fixedScheduleList: List<FixedScheduleItem>
) {
    // 자동 스케줄링된 결과 + 고정 스케줄 중 실제 시간이 있는 항목만 사용
    val scheduleItems = fixedScheduleList.filter {
        it.startTime != null && it.endTime != null
    }

    // 목표 항목만 라벨로 표시
    val goalItems = fixedScheduleList.filter {
        it.category == ScheduleCategory.GOAL &&
                it.startTime != null &&
                it.endTime != null
    }

    val startHour = scheduleItems
        .mapNotNull { it.startTime.toMinutesOrNull() }
        .minOrNull()
        ?.let { floor(it / 60f).toInt() }
        ?: 9

    val endHour = scheduleItems
        .mapNotNull { it.endTime.toMinutesOrNull() }
        .maxOrNull()
        ?.let { ceil(it / 60f).toInt() }
        ?: 18

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Time Table",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (goalItems.isNotEmpty()) {
            GoalLabelRow(goalItems = goalItems)
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items((startHour until endHour).toList()) { hour ->
                HourSlice(
                    hour = hour,
                    schedules = scheduleItems,
                    goals = goalItems,
                    subjectViewModel = subjectViewModel
                )
            }
        }
    }
}

private fun String?.toMinutesOrNull(): Int? {
    if (this.isNullOrBlank()) return null

    val parts = split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}

@Composable
fun GoalLabelRow(
    goalItems: List<FixedScheduleItem>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        goalItems.forEachIndexed { index, goal ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(timetableGoalColors[index % timetableGoalColors.size])
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private val timetableGoalColors = listOf(
    Color(0xFFEAF4FF),
    Color(0xFFE9F7F5),
    Color(0xFFF3F0FF),
    Color(0xFFF0F9FF),
    Color(0xFFF1FAF4)
)
