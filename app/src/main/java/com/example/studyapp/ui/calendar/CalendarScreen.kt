package com.example.studyapp.ui.calendar

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel
) {
    val holidayViewModel: HolidayViewModel = viewModel()

    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var showCalendarDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(selectedDate.year) {
        holidayViewModel.loadKoreanHolidays(selectedDate.year)
    }

    val holidays = holidayViewModel.holidays

    LaunchedEffect(holidays.size) {
        Log.d("HolidayApi", "받아온 공휴일 개수: ${holidays.size}")
        holidays.forEach { holiday ->
            Log.d("HolidayApi", "${holiday.date} / ${holiday.localName}")
        }
    }

    val daySchedules = emptyList<DayScheduleBlock>()

    if (showCalendarDialog) {
        HolidayCalendarDialog(
            selectedDate = selectedDate,
            holidays = holidays,
            onLoadYear = { year ->
                holidayViewModel.loadKoreanHolidays(year)
            },
            onDateSelected = { date ->
                selectedDate = date
            },
            onDismiss = {
                showCalendarDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${selectedDate.year}년 ${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            IconButton(
                onClick = {
                    showCalendarDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "날짜 선택"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DayScheduleTimeline(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            selectedDate = selectedDate,
            schedules = daySchedules
        )
    }
}