package com.example.studyapp.ui.calendar

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studyapp.data.model.GeneratedScheduleItem
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme
import java.time.LocalDate
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel,
    isVisible: Boolean = true
) {
    val holidayViewModel: HolidayViewModel = viewModel()
    val generatedScheduleViewModel: GeneratedScheduleViewModel = viewModel()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val generatedSchedules = generatedScheduleViewModel.schedules
    val fixedScheduleBlocks = generatedScheduleViewModel.fixedScheduleBlocks
    val scheduleMessage = generatedScheduleViewModel.message
    val scheduleSnapshot = generatedSchedules.toList()
    val fixedScheduleSnapshot = fixedScheduleBlocks.toList()
    val timelineSchedules = remember(scheduleSnapshot, fixedScheduleSnapshot) {
        fixedScheduleSnapshot + scheduleSnapshot
    }

    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val scheduledSubjects = remember(
        timelineSchedules,
        selectedDate,
        generatedScheduleViewModel.wakeTime
    ) {
        val wakeStartMinute = generatedScheduleViewModel.wakeTime.toMinutesOrNull() ?: 0

        timelineSchedules
            .filter { it.date == selectedDate }
            .sortedWith(
                compareBy<DayScheduleBlock> {
                    it.startMinuteOfDay().normalizeFrom(wakeStartMinute)
                }
                    .thenBy { it.subject }
            )
            .distinctBy { Pair(it.subject, it.color) }
    }

    var showCalendarDialog by remember {
        mutableStateOf(false)
    }

    var showGroupedScheduleDialog by remember {
        mutableStateOf(false)
    }

    val holidays = holidayViewModel.holidays

    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjectsFromFirestore()
    }


    LaunchedEffect(selectedDate.year) {
        holidayViewModel.loadKoreanHolidays(selectedDate.year)
    }


    fun refreshGeneratedSchedule() {
        generatedScheduleViewModel.generateAndSaveSchedule(
            date = selectedDate,
            showSuccessMessage = false
        )
    }

    LaunchedEffect(selectedDate, isVisible) {
        if (isVisible) {
            refreshGeneratedSchedule()
        }
    }

    DisposableEffect(lifecycleOwner, selectedDate) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshGeneratedSchedule()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                .padding(vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 빈 공간: 높이까지 48로 만들지 말고 width만 유지
            Box(modifier = Modifier.width(80.dp))

            // 중앙 날짜
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedDate.formatKoreanDateWithDay(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 오른쪽 달력 버튼
            Box(
                modifier = Modifier.width(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            showCalendarDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "날짜 선택",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SubjectColorLegend(
                        schedules = scheduledSubjects
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(2.dp))

        DayScheduleTimeline(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            selectedDate = selectedDate,
            schedules = timelineSchedules,
            wakeTime = generatedScheduleViewModel.wakeTime,
            sleepTime = generatedScheduleViewModel.sleepTime
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
        localDate.formatKoreanDateWithDay()
    } catch (e: Exception) {
        date
    }
}

private fun LocalDate.formatKoreanDateWithDay(): String {
    return "${year}년 ${monthValue}월 ${dayOfMonth}일 ${koreanDayOfWeek()}"
}

private fun LocalDate.koreanDayOfWeek(): String {
    return when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "월"
        java.time.DayOfWeek.TUESDAY -> "화"
        java.time.DayOfWeek.WEDNESDAY -> "수"
        java.time.DayOfWeek.THURSDAY -> "목"
        java.time.DayOfWeek.FRIDAY -> "금"
        java.time.DayOfWeek.SATURDAY -> "토"
        java.time.DayOfWeek.SUNDAY -> "일"
    }
}

private fun DayScheduleBlock.startMinuteOfDay(): Int {
    return startHour * 60 + startMinute
}

private fun String.toMinutesOrNull(): Int? {
    val parts = split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23 || minute !in 0..59) return null

    return hour * 60 + minute
}

private fun Int.normalizeFrom(baseStartMinute: Int): Int {
    return if (this < baseStartMinute) {
        this + 24 * 60
    } else {
        this
    }
}

@Composable
private fun SubjectColorLegend(
    modifier: Modifier = Modifier,
    schedules: List<DayScheduleBlock>
) {
    if (schedules.isEmpty()) return
    val isDarkTheme = isAppInDarkTheme()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(schedules) { schedule ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = subjectColorForTheme(schedule.color, isDarkTheme),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = schedule.subject,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
