package com.example.studyapp.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.schedule.FixedScheduleItem
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme

@Composable
fun HourSlice(
    hour: Int,
    schedules: List<FixedScheduleItem>,
    goals: List<FixedScheduleItem>,
    subjectViewModel: SubjectViewModel
){
    val isDarkTheme = isAppInDarkTheme()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = hour.toString().padStart(2, '0'),
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(6) { slotIndex ->
                val slotStartMinute = hour * 60 + (slotIndex * 10)
                val slotEndMinute = slotStartMinute + 10

                val matchedSchedule = schedules.find { schedule ->
                    val start = schedule.startTime?.toMinutes() ?: return@find false
                    val end = schedule.endTime?.toMinutes() ?: return@find false

                    slotStartMinute < end && slotEndMinute > start
                }

                val nearestSchedule = matchedSchedule ?: schedules.minByOrNull { schedule ->
                    val start = schedule.startTime?.toMinutes() ?: return@minByOrNull Int.MAX_VALUE
                    val end = schedule.endTime?.toMinutes() ?: return@minByOrNull Int.MAX_VALUE
                    val slotMiddle = (slotStartMinute + slotEndMinute) / 2

                    when {
                        slotMiddle < start -> start - slotMiddle
                        slotMiddle > end -> slotMiddle - end
                        else -> 0
                    }
                }

                val cellColor = nearestSchedule
                    ?.toScheduleColor(
                        goals = goals,
                        subjectViewModel = subjectViewModel,
                        darkTheme = isDarkTheme
                    )
                    ?.copy(alpha = if (matchedSchedule != null) 1f else 0.32f)
                    ?: Color(0xFFEAF6FF)

                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 50.dp)
                        .padding(horizontal = 2.dp)
                        .background(cellColor)
                )
            }
        }
    }
}

private fun FixedScheduleItem.toScheduleColor(
    goals: List<FixedScheduleItem>,
    subjectViewModel: SubjectViewModel,
    darkTheme: Boolean
): Color {
    val matchedSubject = subjectViewModel.subjects.find { subject ->
        subject.name == title
    }

    val matchedGoalIndex = goals.indexOfFirst { goal ->
        goal.title == title
    }

    return when {
        matchedSubject != null -> subjectColorForTheme(Color(matchedSubject.colorArgb), darkTheme)
        matchedGoalIndex != -1 -> timetableGoalColors[
            matchedGoalIndex % timetableGoalColors.size
        ]
        else -> Color(0xFFBFDFFF)
    }
}

private fun String.toMinutes(): Int? {
    val parts = split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}

private val timetableGoalColors = listOf(
    Color(0xFFEAF4FF),
    Color(0xFFE9F7F5),
    Color(0xFFF3F0FF),
    Color(0xFFF0F9FF),
    Color(0xFFF1FAF4)
)
