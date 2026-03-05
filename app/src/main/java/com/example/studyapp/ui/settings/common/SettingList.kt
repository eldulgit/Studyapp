package com.example.studyapp.ui.settings.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsList(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingItem("목표 설정") {
            navController.navigate("setting_goal")
        }
        SettingItem("알림 설정") {
            navController.navigate("setting_notification")
        }
        SettingItem("테마 설정") {
            navController.navigate("setting_theme")
        }
        SettingItem("AI 프로필 설정") {
            navController.navigate("setting_ai")
        }
        SettingItem("계정 설정") {
            navController.navigate("setting_account")
        }
    }
}
