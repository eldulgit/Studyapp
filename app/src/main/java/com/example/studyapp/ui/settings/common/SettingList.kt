package com.example.studyapp.ui.settings.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.coachHelpTarget

@Composable
fun SettingsList(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(title = "학습 & 스케줄") {
            SettingItem(
                title = "과목 설정",
                icon = Icons.Default.Book,
                modifier = Modifier.coachHelpTarget(CoachHelpTargets.SubjectSettingMenu)
            ) {
                navController.navigate("setting_subject")
            }
            SettingItem(
                title = "생활패턴 설정",
                icon = Icons.Default.Bedtime
            ) {
                navController.navigate("setting_lifestyle")
            }
        }

        SettingsGroup(title = "시스템 & 테마") {
            SettingItem(
                title = "알림 설정",
                icon = Icons.Default.Notifications
            ) {
                navController.navigate("setting_notification")
            }
            SettingItem(
                title = "테마 설정",
                icon = Icons.Default.Palette
            ) {
                navController.navigate("setting_theme")
            }
        }

        SettingsGroup(title = "계정 및 지원") {
            SettingItem(
                title = "계정 설정",
                icon = Icons.Default.AccountCircle
            ) {
                navController.navigate("setting_account")
            }
            SettingItem(
                title = "도움말",
                icon = Icons.Default.Help,
                modifier = Modifier.coachHelpTarget(CoachHelpTargets.HelpMenu)
            ) {
                navController.navigate("setting_help")
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}
