package com.example.studyapp.ui.settings.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.navigation.NavController

@Composable
fun GoalSettingScreen(
    navController: NavController,
    goalViewModel: GoalViewModel
) {
    var dailyHours by remember { mutableStateOf((goalViewModel.dailyGoalMinutes / 60).toString()) }
    var weeklyHours by remember { mutableStateOf((goalViewModel.weeklyGoalMinutes / 60).toString()) }
    var monthlyHours by remember { mutableStateOf((goalViewModel.monthlyGoalMinutes / 60).toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {

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

            Text(
                text = "목표",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(48.dp))
        }


        GoalHourInput("일간 목표 (시간)", dailyHours) { dailyHours = it }
        GoalHourInput("주간 목표 (시간)", weeklyHours) { weeklyHours = it }
        GoalHourInput("월간 목표 (시간)", monthlyHours) { monthlyHours = it }

        Button(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            onClick = {
                val daily = dailyHours.toIntOrNull() ?: 0
                val weekly = weeklyHours.toIntOrNull() ?: 0
                val monthly = monthlyHours.toIntOrNull() ?: 0

                goalViewModel.updateDailyGoal(daily * 60)
                goalViewModel.updateWeeklyGoal(weekly * 60)
                goalViewModel.updateMonthlyGoal(monthly * 60)

                navController.popBackStack()
            }

        ) {
            Text("저장")
        }
    }
}
