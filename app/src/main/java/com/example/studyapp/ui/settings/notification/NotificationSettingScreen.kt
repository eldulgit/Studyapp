package com.example.studyapp.ui.settings.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.settings.SettingsViewModel

@Composable
fun NotificationSettingScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val notificationEnabled = settingsViewModel.notificationEnabled
    val goalAlertEnabled = settingsViewModel.goalAlertEnabled

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "알림",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("공부 알림 받기")
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { settingsViewModel.updateNotificationEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.padding(12.dp))

            NotificationTimePickerRow(
                selectedHour = settingsViewModel.notificationHour,
                selectedMinute = settingsViewModel.notificationMinute,
                onTimeChanged = { hour, minute ->
                    settingsViewModel.updateNotificationTime(hour, minute)
                }
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("목표 미달 시 알림")
                Switch(
                    checked = goalAlertEnabled,
                    onCheckedChange = { settingsViewModel.updateGoalAlertEnabled(it) }
                )
            }
        }
    }
}