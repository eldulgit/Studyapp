package com.example.studyapp.ui.settings.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsList(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingItem("과목 설정") {
            navController.navigate("setting_subject")
        }
        SettingItem("스케줄 설정") {
            navController.navigate("setting_schedule")
        }
        SettingItem("알림 설정") {
            navController.navigate("setting_notification")
        }
        SettingItem("테마 설정") {
            navController.navigate("setting_theme")
        }
        SettingItem("코멘트 설정") {
            navController.navigate("setting_ai")
        }
        SettingItem("계정 설정") {
            navController.navigate("setting_account")
        }
    }
}
