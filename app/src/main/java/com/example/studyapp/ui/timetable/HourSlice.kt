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

@Composable
fun HourSlice(
    hour: Int,
    schedules: List<FixedScheduleItem>,
    subjectViewModel: SubjectViewModel
) {
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
                val slotStartMinute = (hour - 1) * 60 + (slotIndex * 10)
                val slotEndMinute = slotStartMinute + 10

                val matchedSchedule = schedules.find { schedule ->
                    val start = schedule.startTime?.toMinutes() ?: return@find false
                    val end = schedule.endTime?.toMinutes() ?: return@find false

                    slotStartMinute < end && slotEndMinute > start
                }

                val cellColor = matchedSchedule?.let { schedule ->
                    val matchedSubject = subjectViewModel.subjects.find { subject ->
                        subject.name == schedule.title
                    }

                    matchedSubject?.let { Color(it.colorArgb) } ?: Color.LightGray
                } ?: Color(0xFFF1F1F1)

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

private fun String.toMinutes(): Int? {
    val parts = split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}