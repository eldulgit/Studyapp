package com.example.studyapp.ui.settings.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.coachHelpTarget

@Composable
fun SettingsList(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingItem(
            title = "과목 설정",
            modifier = Modifier.coachHelpTarget(CoachHelpTargets.SubjectSettingMenu)
        ) {
            navController.navigate("setting_subject")
        }
        SettingItem("생활패턴 설정") {
            navController.navigate("setting_lifestyle")
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
        SettingItem(
            title = "도움말",
            modifier = Modifier.coachHelpTarget(CoachHelpTargets.HelpMenu)
        ) {
            navController.navigate("setting_help")
        }
    }
}
