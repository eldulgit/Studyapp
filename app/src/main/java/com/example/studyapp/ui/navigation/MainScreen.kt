package com.example.studyapp.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyapp.ui.calendar.CalendarScreen
import com.example.studyapp.ui.settings.SettingsViewModel
import com.example.studyapp.ui.settings.account.AccountSettingScreen
import com.example.studyapp.ui.settings.ai.AiProfileSettingScreen
import com.example.studyapp.ui.settings.common.SettingScreen
import com.example.studyapp.ui.settings.lifestyle.LifeStyleSettingScreen
import com.example.studyapp.ui.settings.notification.NotificationSettingScreen
import com.example.studyapp.ui.settings.schedule.ScheduleSettingScreen
import com.example.studyapp.ui.settings.subject.SubjectSettingScreen
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.settings.theme.ThemeSettingScreen
import com.example.studyapp.ui.stats.StatsScreen
import com.example.studyapp.ui.timer.TimerScreen
import com.example.studyapp.ui.timer.TimerViewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.studyapp.notification.StudyNotificationScheduler
import com.example.studyapp.ui.settings.lifestyle.LifeStyleViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    var exactAlarmPermissionRequested by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        settingsViewModel.updateNotificationEnabled(granted)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = listOf(
        BottomNavItem.Calendar.route,
        BottomNavItem.Timer.route,
        BottomNavItem.Stats.route,
        BottomNavItem.Setting.route
    )

    LaunchedEffect(currentRoute) {
        if (currentRoute == BottomNavItem.Timer.route && timerViewModel.runningTaskId == null) {
            timerViewModel.loadTodayGeneratedScheduleTimersFromDb()
        }
    }

    LaunchedEffect(settingsViewModel.notificationEnabled) {
        if (
            settingsViewModel.notificationEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (
            settingsViewModel.notificationEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !StudyNotificationScheduler.canScheduleExactAlarms(context) &&
            !exactAlarmPermissionRequested
        ) {
            exactAlarmPermissionRequested = true

            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }

            runCatching {
                context.startActivity(intent)
            }
        }
    }

    DisposableEffect(lifecycleOwner, settingsViewModel.notificationEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && settingsViewModel.notificationEnabled) {
                settingsViewModel.refreshStudyReminderSchedule()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                    studiedMinutes = timerViewModel.studiedMinutes,
                    commentOption = settingsViewModel.commentOption
                )
            }

            composable(BottomNavItem.Timer.route) {
                TimerScreen(
                    timerViewModel = timerViewModel,
                    subjectViewModel = subjectViewModel,
                    settingsViewModel = settingsViewModel
                )
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

            composable("setting_lifestyle") {
                val lifeStyleViewModel: LifeStyleViewModel = viewModel()

                LaunchedEffect(Unit) {
                    lifeStyleViewModel.loadLifestyle()
                }

                LaunchedEffect(lifeStyleViewModel.saveCompleted) {
                    if (lifeStyleViewModel.saveCompleted) {
                        lifeStyleViewModel.consumeSaveCompleted()
                        navController.popBackStack()
                    }
                }

                LifeStyleSettingScreen(
                    initialWakeTime = lifeStyleViewModel.wakeTime,
                    initialSleepTime = lifeStyleViewModel.sleepTime,
                    initialLunchStartTime = lifeStyleViewModel.lunchStartTime,
                    initialLunchEndTime = lifeStyleViewModel.lunchEndTime,
                    initialDinnerStartTime = lifeStyleViewModel.dinnerStartTime,
                    initialDinnerEndTime = lifeStyleViewModel.dinnerEndTime,
                    onSaveClick = lifeStyleViewModel::saveLifestyle,
                    onBackClick = { navController.popBackStack() }
                )
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
                AccountSettingScreen(
                    navController = navController,
                    onLogout = onLogout
                )
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
