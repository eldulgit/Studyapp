package com.example.studyapp.ui.calendar

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studyapp.data.model.GeneratedScheduleItem
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel
) {
    val holidayViewModel: HolidayViewModel = viewModel()
    val generatedScheduleViewModel: GeneratedScheduleViewModel = viewModel()

    val context = LocalContext.current

    val generatedSchedules = generatedScheduleViewModel.schedules
    val isGenerating = generatedScheduleViewModel.isGenerating
    val scheduleMessage = generatedScheduleViewModel.message

    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var showCalendarDialog by remember {
        mutableStateOf(false)
    }

    var showGroupedScheduleDialog by remember {
        mutableStateOf(false)
    }

    val holidays = holidayViewModel.holidays

    LaunchedEffect(selectedDate.year) {
        holidayViewModel.loadKoreanHolidays(selectedDate.year)
    }

    LaunchedEffect(holidays.size) {
        Log.d("HolidayApi", "받아온 공휴일 개수: ${holidays.size}")
        holidays.forEach { holiday ->
            Log.d("HolidayApi", "${holiday.date} / ${holiday.localName}")
        }
    }

    LaunchedEffect(selectedDate) {
        generatedScheduleViewModel.loadSchedules(selectedDate)
    }

    LaunchedEffect(scheduleMessage) {
        scheduleMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            generatedScheduleViewModel.clearMessage()
        }
    }

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

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            generatedScheduleViewModel.generateAndSaveSchedule(selectedDate)
                        },
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "시간표 생성")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
            schedules = generatedSchedules
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupedScheduleDialog(
    groupedSchedules: Map<String, List<GeneratedScheduleItem>>,
    onDateClick: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "저장된 시간표")
        },
        text = {
            if (groupedSchedules.isEmpty()) {
                Text(text = "저장된 시간표가 없습니다.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedSchedules
                        .toSortedMap(compareByDescending { it })
                        .forEach { (date, schedules) ->
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val localDate = runCatching {
                                                LocalDate.parse(date)
                                            }.getOrNull()

                                            if (localDate != null) {
                                                onDateClick(localDate)
                                            }
                                        }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = formatScheduleDate(date),
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    schedules
                                        .sortedBy { it.startTime }
                                        .forEach { schedule ->
                                            Text(
                                                text = "${schedule.startTime} ~ ${schedule.endTime}  ${schedule.title}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                }
                            }
                        }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "닫기")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatScheduleDate(date: String): String {
    return try {
        val localDate = LocalDate.parse(date)
        "${localDate.year}년 ${localDate.monthValue}월 ${localDate.dayOfMonth}일"
    } catch (e: Exception) {
        date
    }
}