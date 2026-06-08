package com.example.studyapp.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.studyapp.ui.settings.help.HelpGuideScreen
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
import kotlinx.coroutines.delay
import com.example.studyapp.ui.help.CoachHelpOverlay
import com.example.studyapp.ui.help.CoachHelpHighlight
import com.example.studyapp.ui.help.CoachHelpHighlightShape
import com.example.studyapp.ui.help.CoachHelpPlacement
import com.example.studyapp.ui.help.CoachHelpStep
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.LocalCoachHelpTargetState
import com.example.studyapp.ui.help.rememberCoachHelpTargetState
import com.example.studyapp.ui.settings.lifestyle.LifeStyleViewModel
import com.example.studyapp.util.isIgnoringBatteryOptimizations
import com.example.studyapp.util.openBatteryOptimizationSettings

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
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations())
    }
    var requestedBatteryOptimizationThisSession by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            settingsViewModel.updateNotificationEnabled(false)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = listOf(
        BottomNavItem.ScheduleSetting.route,
        BottomNavItem.Timer.route,
        BottomNavItem.Calendar.route,
        BottomNavItem.Stats.route,
        BottomNavItem.Setting.route
    )
    val coachHelpTargetState = rememberCoachHelpTargetState()
    val coachHelpPrefs = remember {
        context.getSharedPreferences("coach_help_prefs", Context.MODE_PRIVATE)
    }
    val shouldShowCoachHelpOnFirstRun = remember {
        !coachHelpPrefs.getBoolean("coach_help_completed", false)
    }
    val coachHelpSteps = remember {
        listOf(
            CoachHelpStep(
                route = BottomNavItem.ScheduleSetting.route,
                title = "스케줄과 목표 추가",
                description = "+ 버튼으로 일정과 목표를 추가해요.",
                placement = CoachHelpPlacement.Bottom,
                highlight = circleHighlight(0.90f, 0.84f, 0.15f),
                targetKey = CoachHelpTargets.ScheduleSettingAdd
            ),
            CoachHelpStep(
                route = BottomNavItem.Setting.route,
                title = "과목설정",
                description = "과목 정보와 색상을 저장해요.",
                placement = CoachHelpPlacement.Center,
                highlight = rectHighlight(0.15f, 0.275f, 0.25f, 0.048f),
                targetKey = CoachHelpTargets.SubjectSettingMenu
            ),
            CoachHelpStep(
                route = BottomNavItem.Calendar.route,
                title = "스케줄링",
                description = "생성된 공부 계획을 확인해요.",
                placement = CoachHelpPlacement.Top,
                highlight = null
            ),
            CoachHelpStep(
                route = BottomNavItem.Timer.route,
                title = "Timer",
                description = "재생 후 카메라로 측정해요.",
                placement = CoachHelpPlacement.Top,
                highlight = circleHighlight(0.89f, 0.07f, 0.085f),
                targetKey = CoachHelpTargets.TimerCamera
            ),
        )
    }
    var showCoachHelp by remember { mutableStateOf(shouldShowCoachHelpOnFirstRun) }
    var showCoachHelpOverlay by remember { mutableStateOf(false) }
    var coachHelpIndex by remember { mutableLongStateOf(0L) }
    val currentCoachStep = coachHelpSteps.getOrNull(coachHelpIndex.toInt())

    LaunchedEffect(showCoachHelp, currentCoachStep?.route) {
        if (!showCoachHelp) return@LaunchedEffect

        val targetRoute = currentCoachStep?.route ?: return@LaunchedEffect
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.id) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == BottomNavItem.Timer.route && timerViewModel.runningTaskId == null) {
            timerViewModel.loadTodayGeneratedScheduleTimersFromDb()
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.loadNotificationSettingsFromDb()
    }

    LaunchedEffect(
        settingsViewModel.notificationSettingsLoaded,
        settingsViewModel.notificationEnabled,
        settingsViewModel.goalAlertEnabled,
        isIgnoringBatteryOptimizations,
        requestedBatteryOptimizationThisSession
    ) {
        if (
            settingsViewModel.notificationSettingsLoaded &&
            (settingsViewModel.notificationEnabled || settingsViewModel.goalAlertEnabled) &&
            !isIgnoringBatteryOptimizations &&
            !requestedBatteryOptimizationThisSession
        ) {
            requestedBatteryOptimizationThisSession = true
            context.openBatteryOptimizationSettings()
        }

        if (
            settingsViewModel.notificationSettingsLoaded &&
            (settingsViewModel.notificationEnabled || settingsViewModel.goalAlertEnabled) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(lifecycleOwner, settingsViewModel.notificationEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations()
                if (settingsViewModel.notificationEnabled) {
                    settingsViewModel.refreshStudyReminderSchedule()
                }
                settingsViewModel.refreshGoalReminderSchedule()
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
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalCoachHelpTargetState provides coachHelpTargetState
            ) {
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.ScheduleSetting.route,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(BottomNavItem.ScheduleSetting.route) {
                        ScheduleSettingScreen()
                    }

                    composable(BottomNavItem.Calendar.route) {
                        CalendarScreen(
                            navController = navController,
                            subjectViewModel = subjectViewModel,
                            isVisible = currentRoute == BottomNavItem.Calendar.route
                        )
                    }

                    composable(BottomNavItem.Stats.route) {
                        StatsScreen(
                            studiedMinutes = timerViewModel.studiedMinutes,
                            commentOption = settingsViewModel.commentOption,
                            subjectViewModel = subjectViewModel
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

                    composable("setting_help") {
                        HelpGuideScreen(navController)
                    }
                }

                if (
                    showCoachHelp &&
                    showCoachHelpOverlay &&
                    currentCoachStep != null &&
                    currentRoute == currentCoachStep.route
                ) {
                    CoachHelpOverlay(
                        step = currentCoachStep,
                        currentStepIndex = coachHelpIndex.toInt(),
                        totalStepCount = coachHelpSteps.size,
                        contentPadding = padding,
                        targetBounds = coachHelpTargetState.boundsFor(currentCoachStep.targetKey),
                        onNext = {
                            val nextIndex = coachHelpIndex + 1
                            showCoachHelpOverlay = false
                            if (nextIndex < coachHelpSteps.size) {
                                coachHelpIndex = nextIndex
                            } else {
                                showCoachHelp = false
                                coachHelpPrefs.edit()
                                    .putBoolean("coach_help_completed", true)
                                    .apply()
                                navController.navigate(BottomNavItem.ScheduleSetting.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(
        showCoachHelp,
        coachHelpIndex,
        currentRoute,
        currentCoachStep?.route
    ) {
        showCoachHelpOverlay = false

        if (
            showCoachHelp &&
            currentCoachStep != null &&
            currentRoute == currentCoachStep.route
        ) {
            delay(220)
            showCoachHelpOverlay = true
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

private fun circleHighlight(
    centerXRatio: Float,
    centerYRatio: Float,
    diameterRatio: Float
): CoachHelpHighlight {
    return CoachHelpHighlight(
        shape = CoachHelpHighlightShape.Circle,
        centerXRatio = centerXRatio,
        centerYRatio = centerYRatio,
        widthRatio = diameterRatio,
        heightRatio = diameterRatio
    )
}

private fun rectHighlight(
    centerXRatio: Float,
    centerYRatio: Float,
    widthRatio: Float,
    heightRatio: Float
): CoachHelpHighlight {
    return CoachHelpHighlight(
        shape = CoachHelpHighlightShape.RoundRect,
        centerXRatio = centerXRatio,
        centerYRatio = centerYRatio,
        widthRatio = widthRatio,
        heightRatio = heightRatio
    )
}
