package com.example.studyapp.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.studyapp.ui.settings.goal.GoalViewModel

@Composable
fun StatsScreen(
    navController: NavHostController,
    goalViewModel: GoalViewModel,
    studiedMinutes: Int
) {

    var selectedPeriod by remember { mutableStateOf(StatsPeriod.DAILY) }

    val goal = when (selectedPeriod) {
        StatsPeriod.DAILY -> goalViewModel.dailyGoalMinutes
        StatsPeriod.WEEKLY -> goalViewModel.weeklyGoalMinutes
        StatsPeriod.MONTHLY -> goalViewModel.monthlyGoalMinutes
    }

    val percent = if (goal > 0) {
        (studiedMinutes * 100) / goal
    } else 0

    val comment = getStudyComment(
        percent = percent,
        period = selectedPeriod
    )

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text("Stats", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(modifier = Modifier.padding(16.dp)) {

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

