package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studyapp.data.model.Holiday
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HolidayCalendarDialog(
    selectedDate: LocalDate,
    holidays: List<Holiday>,
    onLoadYear: (Int) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var visibleMonth by remember {
        mutableStateOf(YearMonth.from(selectedDate))
    }

    LaunchedEffect(visibleMonth.year) {
        onLoadYear(visibleMonth.year)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "닫기")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        visibleMonth = visibleMonth.minusMonths(1)
                    }
                ) {
                    Text(text = "<")
                }

                Text(
                    text = "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        visibleMonth = visibleMonth.plusMonths(1)
                    }
                ) {
                    Text(text = ">")
                }
            }
        },
        text = {
            Column {
                WeekHeader()

                Spacer(modifier = Modifier.height(8.dp))

                MonthGrid(
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    holidays = holidays,
                    onDateSelected = { date ->
                        onDateSelected(date)
                        onDismiss()
                    }
                )
            }
        }
    )
}

@Composable
private fun WeekHeader() {
    val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    holidays: List<Holiday>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = visibleMonth.atDay(1)
    val lastDayOfMonth = visibleMonth.lengthOfMonth()

    val firstDayOffset = dayOfWeekToCalendarIndex(firstDayOfMonth.dayOfWeek)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (week in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (dayIndex in 0 until 7) {
                    val cellIndex = week * 7 + dayIndex
                    val dayNumber = cellIndex - firstDayOffset + 1

                    if (dayNumber in 1..lastDayOfMonth) {
                        val date = visibleMonth.atDay(dayNumber)

                        val isHoliday = holidays.any { holiday ->
                            holiday.date == date.toString()
                        }

                        DayCell(
                            modifier = Modifier.weight(1f),
                            date = date,
                            isSelected = date == selectedDate,
                            isHoliday = isHoliday,
                            onClick = {
                                onDateSelected(date)
                            }
                        )
                    } else {
                        EmptyDayCell(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isSelected: Boolean,
    isHoliday: Boolean,
    onClick: () -> Unit
) {
    val dateTextColor = if (isHoliday) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val selectedBackgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = selectedBackgroundColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dateTextColor,
                fontWeight = if (isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}

@Composable
private fun EmptyDayCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(58.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun dayOfWeekToCalendarIndex(dayOfWeek: DayOfWeek): Int {
    return when (dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}
