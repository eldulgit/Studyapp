package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.data.repository.StudyRepository
import com.example.studyapp.data.repository.StudyRepository.getStudyMinutesForDate
import com.example.studyapp.ui.settings.goal.GoalViewModel
import java.time.LocalDate
import java.time.YearMonth


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(navController: NavController, goalViewModel: GoalViewModel) {

    val goalMinutes = goalViewModel.dailyGoalMinutes


    var currentMonth by remember {
        mutableStateOf(YearMonth.of(2026, 1))
    }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val dayTotalMinutes = selectedDate?.let{
        getStudyMinutesForDate(it)
    } ?: 0
    val monthTotalMinutes = StudyRepository.getMonthTotal(currentMonth)

    val strongColor = MaterialTheme.colorScheme.primary
    val mediumColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val lightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)


    fun getColorForStudyMinutes(
        minutes: Int,
        goalMinutes: Int,
        strong: Color,
        medium: Color,
        light: Color
    ): Color {
        val percent = minutes.toFloat() / goalMinutes * 100f

        return when {
            percent >= 80 -> strong
            percent >= 40 -> medium
            percent > 0 -> light
            else -> Color.Transparent
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = {
                    navController.navigate("stats")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = "Stats"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
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
            Text(text = "${currentMonth.year}Y ${String.format("%02d",currentMonth.monthValue)}M",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp))
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


        Text(
            text = "GOAL = $goalMinutes",
            modifier = Modifier.padding(16.dp)
        )


        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            WeekDayRow()

            Spacer(modifier = Modifier.height(8.dp))

            key(goalMinutes) {
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    goalMinutes = goalMinutes,
                    onDateSelected = { selectedDate = it },
                    getDayColor = { date ->
                        val minutes = date?.let {
                            StudyRepository.getStudyMinutesForDate(it)
                        } ?: 0

                        getColorForStudyMinutes(
                            minutes,
                            goalMinutes,
                            strongColor,
                            mediumColor,
                            lightColor
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                text = selectedDate?.let {
                    "Day Total : ${dayTotalMinutes / 60}H ${dayTotalMinutes % 60}M"
                } ?: "날짜를 선택하세요",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Month Total : ${monthTotalMinutes / 60}H ${monthTotalMinutes % 60}M",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
