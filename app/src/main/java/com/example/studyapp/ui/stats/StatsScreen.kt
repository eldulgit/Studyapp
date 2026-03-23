package com.example.studyapp.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.subject.SubjectViewModel

@Composable
fun StatsScreen(
    subjectViewModel: SubjectViewModel,
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

    Column(modifier = Modifier.fillMaxSize()) {

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

            Spacer(modifier = Modifier.height(24.dp))

            StatsCommentSection(comment = comment)
        }
    }
}

