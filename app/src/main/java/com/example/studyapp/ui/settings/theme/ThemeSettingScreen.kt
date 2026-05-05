package com.example.studyapp.ui.settings.theme

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.settings.SettingsViewModel

@Composable
fun ThemeSettingScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val selectedTheme = settingsViewModel.selectedTheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
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
                text = "테마",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "테마 선택",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ThemeOption(
                text = "라이트 모드",
                selected = selectedTheme == "light",
                onClick = { settingsViewModel.updateTheme("light") }
            )

            ThemeOption(
                text = "다크 모드",
                selected = selectedTheme == "dark",
                onClick = { settingsViewModel.updateTheme("dark") }
            )

            ThemeOption(
                text = "시스템 설정 따르기",
                selected = selectedTheme == "system",
                onClick = { settingsViewModel.updateTheme("system") }
            )
        }
    }
}