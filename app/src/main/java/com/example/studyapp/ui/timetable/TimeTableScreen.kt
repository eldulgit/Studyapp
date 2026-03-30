package com.example.studyapp.ui.timetable

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.studyapp.ui.schedule.ScheduleViewModel
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studyapp.ui.settings.schedule.FixedScheduleItem
import com.example.studyapp.ui.settings.schedule.ScheduleCategory
import com.example.studyapp.ui.settings.subject.SubjectViewModel

@Composable
fun TimeTableScreen(
    subjectViewModel: SubjectViewModel,
    fixedScheduleList: List<FixedScheduleItem>
) {
    val scheduleViewModel: ScheduleViewModel = viewModel()
    LaunchedEffect(Unit) {
        scheduleViewModel.loadSchedulesFromFirestore()
    }

    val startHour = 1
    val endHour = 24

    val localScheduleItems = fixedScheduleList.filter {
        it.category == ScheduleCategory.SCHEDULE
    }

    val firestoreScheduleItems = scheduleViewModel.schedules.mapNotNull { schedule ->
        val subject = subjectViewModel.subjects.find{ it.id == schedule.subjectId }
            ?: return@mapNotNull null

        FixedScheduleItem(
            id = schedule.id.hashCode().toLong(),
            category = ScheduleCategory.SCHEDULE,
            title = subject.name,
            dayOfWeek = schedule.dayOfWeek,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
        )
    }
    val allScheduleItems = localScheduleItems + firestoreScheduleItems

    // 스케줄 카테고리만 필터
//    val scheduleItems = fixedScheduleList.filter {
//        it.category == ScheduleCategory.SCHEDULE
//    }

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
                    schedules = localScheduleItems,
                    subjectViewModel = subjectViewModel
                )
            }
        }
    }
}