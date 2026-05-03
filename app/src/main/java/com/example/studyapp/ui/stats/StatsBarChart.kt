package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsBarChart(
    period: StatsPeriod,
    records: List<StudySessionRecord>
) {
    val labels = generateLabels(period)

    val values = generateBarValues(
        records = records,
        period = period,
        labelCount = labels.size
    )

    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEach { value ->
                val barHeight = ((value.toFloat() / maxValue.toFloat()) * 160).dp

                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(barHeight)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateBarValues(
    records: List<StudySessionRecord>,
    period: StatsPeriod,
    labelCount: Int
): List<Int> {
    val today = LocalDate.now()

    return when (period) {
        StatsPeriod.DAILY -> {
            val startDate = today.minusDays((labelCount - 1).toLong())

            List(labelCount) { index ->
                val date = startDate.plusDays(index.toLong()).toString()

                secondsToChartValue(
                    records
                        .filter { it.sessionDate == date }
                        .sumOf { it.studiedSeconds }
                )
            }
        }

        StatsPeriod.WEEKLY -> {
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            List(labelCount) { index ->
                val date = startOfWeek.plusDays(index.toLong()).toString()

                secondsToChartValue(
                    records
                        .filter { it.sessionDate == date }
                        .sumOf { it.studiedSeconds }
                )
            }
        }

        StatsPeriod.MONTHLY -> {
            val startMonth = today
                .minusMonths((labelCount - 1).toLong())
                .withDayOfMonth(1)

            List(labelCount) { index ->
                val targetMonth = startMonth.plusMonths(index.toLong())

                secondsToChartValue(
                    records
                        .filter { record ->
                            val recordDate = LocalDate.parse(record.sessionDate)

                            recordDate.year == targetMonth.year &&
                                    recordDate.monthValue == targetMonth.monthValue
                        }
                        .sumOf { it.studiedSeconds }
                )
            }
        }
    }
}

private fun secondsToChartValue(seconds: Int): Int {
    return if (seconds > 0 && seconds < 60) {
        1
    } else {
        seconds / 60
    }
}