package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    studiedMinutes: Int
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.DAILY) }

    val goal = when (selectedPeriod) {
        StatsPeriod.DAILY -> 120
        StatsPeriod.WEEKLY -> 600
        StatsPeriod.MONTHLY -> 2400
    }

    val percent = if (goal > 0) {
        (studiedMinutes * 100) / goal
    } else 0

    val comment = getStudyComment(
        percent = percent,
        period = selectedPeriod
    )

    val records by StudySessionRepository.records.collectAsState()

    val scheduledHours = remember {
        listOf(9, 14, 16, 20)
    }

    val hourlyFocusPoints = remember(records, scheduledHours) {
        generateHourlyFocusData(
            records = records,
            scheduledHours = scheduledHours
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StatsFilterRow(
                selected = selectedPeriod,
                onSelect = { selectedPeriod = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            StatsBarChart(
                period = selectedPeriod
            )

            Spacer(modifier = Modifier.height(32.dp))

            StudyFocusLineChart(
                points = hourlyFocusPoints
            )

            Spacer(modifier = Modifier.height(24.dp))

            StatsCommentSection(comment = comment)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}