package com.example.studyapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyapp.ui.calendar.CalendarScreen
import com.example.studyapp.ui.settings.account.AccountSettingScreen
import com.example.studyapp.ui.settings.ai.AiProfileSettingScreen
import com.example.studyapp.ui.settings.common.SettingScreen
import com.example.studyapp.ui.settings.goal.GoalSettingScreen
import com.example.studyapp.ui.settings.goal.GoalViewModel
import com.example.studyapp.ui.settings.notification.NotificationSettingScreen
import com.example.studyapp.ui.settings.theme.ThemeSettingScreen
import com.example.studyapp.ui.stats.StatsScreen
import com.example.studyapp.ui.timer.TimerScreen
import com.example.studyapp.ui.timer.TimerViewModel
import com.example.studyapp.ui.timetable.TimeTableScreen
import com.example.studyapp.ui.todo.TodoScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val goalViewModel: GoalViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Todo.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(BottomNavItem.Calendar.route) {
                CalendarScreen(navController, goalViewModel)
            }

            composable("stats") {
                StatsScreen(
                    navController = navController,
                    goalViewModel = goalViewModel,
                    studiedMinutes = timerViewModel.studiedMinutes
                )
            }

            composable(BottomNavItem.Todo.route) {
                TodoScreen()
            }

            composable(BottomNavItem.Timer.route) {
                TimerScreen(
                    navController = navController,
                    timerViewModel = timerViewModel
                )
            }

            composable("Time Table") {
                TimeTableScreen()
            }

            composable(BottomNavItem.Setting.route) {
                SettingScreen(navController)
            }

            composable("setting_goal") {
                GoalSettingScreen(navController, goalViewModel)
            }

            composable("setting_notification") {
                NotificationSettingScreen(navController)
            }

            composable("setting_theme") {
                ThemeSettingScreen(navController)
            }

            composable("setting_ai") {
                AiProfileSettingScreen(navController)
            }

            composable("setting_account") {
                AccountSettingScreen(navController)
            }
        }
    }
}
