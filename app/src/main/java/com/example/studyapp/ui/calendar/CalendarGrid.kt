package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    goalMinutes: Int,
    onDateSelected: (LocalDate) -> Unit,
    getDayColor: (LocalDate?) -> Color
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = startOffset + lastDayOfMonth.dayOfMonth

    val dates: List<LocalDate?> = (0 until totalDays).map { index ->
        if (index < startOffset) null
        else yearMonth.atDay(index - startOffset + 1)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(dates.size) { i ->
            val date = dates[i]

            CalendarDayCell(
                date = date,
                isSelected = date != null && date == selectedDate,
                backgroundColor = getDayColor(date),
                onClick = {
                    date?.let { onDateSelected(it) }
                }
            )
        }
    }
}
