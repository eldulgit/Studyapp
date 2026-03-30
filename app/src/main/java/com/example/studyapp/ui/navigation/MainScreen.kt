package com.example.studyapp.ui.navigation

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyapp.ui.calendar.CalendarScreen
import com.example.studyapp.ui.camera.CameraScreen
import com.example.studyapp.ui.settings.SettingsViewModel
import com.example.studyapp.ui.settings.account.AccountSettingScreen
import com.example.studyapp.ui.settings.ai.AiProfileSettingScreen
import com.example.studyapp.ui.settings.common.SettingScreen
import com.example.studyapp.ui.settings.notification.NotificationSettingScreen
import com.example.studyapp.ui.settings.schedule.ScheduleSettingScreen
import com.example.studyapp.ui.settings.subject.SubjectSettingScreen
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.settings.theme.ThemeSettingScreen
import com.example.studyapp.ui.stats.StatsScreen
import com.example.studyapp.ui.timer.TimerScreen
import com.example.studyapp.ui.timer.TimerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val context = LocalContext.current
    val activity = context as? Activity

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = listOf(
        BottomNavItem.Calendar.route,
        BottomNavItem.Timer.route,
        BottomNavItem.Stats.route,
        BottomNavItem.Setting.route
    )

    var lastBackPressedTime by remember { mutableLongStateOf(0L) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Calendar.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Calendar.route) {
                CalendarScreen(navController, subjectViewModel)
            }

            composable(BottomNavItem.Stats.route) {
                StatsScreen(
                    studiedMinutes = timerViewModel.studiedMinutes
                )
            }

            composable(BottomNavItem.Timer.route) {
                TimerScreen(
                    timerViewModel = timerViewModel,
                    subjectViewModel = subjectViewModel,
                    navController = navController
                )
            }

            composable("camera") {
                CameraScreen()
            }

            composable(BottomNavItem.Setting.route) {
                SettingScreen(navController)
            }

            composable("setting_subject") {
                SubjectSettingScreen(navController, subjectViewModel)
            }

            composable("setting_schedule") {
                ScheduleSettingScreen(navController)
            }

            composable("setting_notification") {
                NotificationSettingScreen(navController, settingsViewModel)
            }

            composable("setting_theme") {
                ThemeSettingScreen(navController, settingsViewModel)
            }

            composable("setting_ai") {
                AiProfileSettingScreen(navController, settingsViewModel)
            }

            composable("setting_account") {
                AccountSettingScreen(navController)
            }
        }
    }

    BackHandler {
        val now = System.currentTimeMillis()

        if (currentRoute in bottomRoutes) {
            if (now - lastBackPressedTime <= 1500L) {
                activity?.finish()
            } else {
                lastBackPressedTime = now
                Toast.makeText(
                    context,
                    "한 번 더 누르면 종료됩니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            navController.popBackStack()
        }
    }
}