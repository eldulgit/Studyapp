package com.example.studyapp.ui.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.schedule.FixedScheduleItem
import com.example.studyapp.ui.settings.schedule.ScheduleCategory
import com.example.studyapp.ui.settings.subject.SubjectViewModel

@Composable
fun TimeTableScreen(
    subjectViewModel: SubjectViewModel,
    fixedScheduleList: List<FixedScheduleItem>
) {
    val startHour = 1
    val endHour = 24

    // 스케줄 카테고리만 필터
    val scheduleItems = fixedScheduleList.filter {
        it.category == ScheduleCategory.SCHEDULE
    }

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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items((startHour..endHour).toList()) { hour ->
                HourSlice(
                    hour = hour,
                    schedules = scheduleItems,
                    subjectViewModel = subjectViewModel
                )
            }
        }
    }
}