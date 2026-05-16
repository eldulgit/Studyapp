package com.example.studyapp.ui.settings.notification

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.settings.SettingsViewModel

@Composable
fun NotificationSettingScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val notificationEnabled = settingsViewModel.notificationEnabled
    val drowsinessAlertEnabled = settingsViewModel.drowsinessAlertEnabled

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
                SkyOutlineSwitch(
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
                Text("졸음 감지 알림")

                SkyOutlineSwitch(
                    checked = drowsinessAlertEnabled,
                    onCheckedChange = {
                        settingsViewModel.updateDrowsinessAlertEnabled(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun SkyOutlineSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val trackBorderColor = if (checked) accentColor else MaterialTheme.colorScheme.outlineVariant
    val thumbColor = if (checked) accentColor else MaterialTheme.colorScheme.outlineVariant
    val thumbOffset = animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        label = "NotificationSwitchThumbOffset"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 2.dp,
                color = trackBorderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.value)
                .size(18.dp)
                .background(
                    color = thumbColor,
                    shape = CircleShape
                )
        )
    }
}
