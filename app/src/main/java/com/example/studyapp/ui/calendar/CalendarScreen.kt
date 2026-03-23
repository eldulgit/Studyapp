package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import java.time.LocalDate
import java.time.YearMonth


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(navController: NavController, subjectViewModel: SubjectViewModel) {

    var currentMonth by remember {
        mutableStateOf(YearMonth.now())
    }
    var selectedDate by remember {
        mutableStateOf(
            LocalDate.now().takeIf { YearMonth.from(it) == currentMonth }
        )
    }
    val daySchedules = emptyList<DayScheduleBlock>()

    LaunchedEffect(currentMonth) {
        if (selectedDate == null || YearMonth.from(selectedDate) != currentMonth) {
            selectedDate = if (currentMonth == YearMonth.now()) {
                LocalDate.now()
            } else {
                currentMonth.atDay(1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth = currentMonth.minusMonths(1)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month"
                    )
                }
                Text(
                    text = "${currentMonth.year}Y ${String.format("%02d", currentMonth.monthValue)}M",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = {
                        currentMonth = currentMonth.plusMonths(1)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            WeekDayRow()

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DayScheduleTimeline(
            modifier = Modifier.weight(1f),
            selectedDate = selectedDate,
            schedules = daySchedules
        )
    }
}