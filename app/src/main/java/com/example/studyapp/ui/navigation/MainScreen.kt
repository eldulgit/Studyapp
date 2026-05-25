package com.example.studyapp.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
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
import com.example.studyapp.ui.help.CoachHelpOverlay
import com.example.studyapp.ui.help.CoachHelpPlacement
import com.example.studyapp.ui.help.CoachHelpStep
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
        settingsViewModel.updateNotificationEnabled(granted)
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
    val coachHelpSteps = remember {
        listOf(
            CoachHelpStep(
                route = BottomNavItem.Setting.route,
                title = "Settings에서 과목 추가",
                description = "먼저 Settings 탭의 과목 설정에서 공부할 과목을 추가해요. 과목 색상은 스케줄, 타이머, 통계 화면에서 같은 색으로 표시돼요.",
                placement = CoachHelpPlacement.Bottom
            ),
            CoachHelpStep(
                route = "setting_subject",
                title = "과목 설정",
                description = "여기서 과목명, 색상, 우선순위를 정해요. 과목을 먼저 만들어두면 이후 스케줄 생성과 타이머가 훨씬 자연스럽게 연결돼요.",
                placement = CoachHelpPlacement.Bottom
            ),
            CoachHelpStep(
                route = BottomNavItem.ScheduleSetting.route,
                title = "Setup에서 스케줄 추가",
                description = "Setup 탭에서는 오른쪽 아래 + 버튼으로 목표나 고정 스케줄을 추가해요. 학교 수업처럼 매주 반복되는 시간은 스케줄로 넣으면 돼요.",
                placement = CoachHelpPlacement.Bottom
            ),
            CoachHelpStep(
                route = BottomNavItem.ScheduleSetting.route,
                title = "목표 우선순위",
                description = "목표 항목의 체크를 켜면 마감일이 가까워질수록 우선순위가 올라가요. 시험이나 과제처럼 시간이 지날수록 더 중요해지는 목표에 사용해요.",
                placement = CoachHelpPlacement.Top
            ),
            CoachHelpStep(
                route = BottomNavItem.Calendar.route,
                title = "Schedule 생성 버튼",
                description = "Schedule 탭 오른쪽 위에서 달력 아이콘 옆의 생성 아이콘을 누르면 오늘 스케줄이 만들어져요. 생활패턴, 과목, 목표, 고정 스케줄을 기준으로 배치돼요.",
                placement = CoachHelpPlacement.Top
            ),
            CoachHelpStep(
                route = BottomNavItem.Timer.route,
                title = "Timer 자동 추가",
                description = "스케줄을 생성하면 Timer 탭에 오늘 공부할 시간이 과목별로 자동 추가돼요. 공부하다 줄어든 시간은 오늘 기준으로 저장돼서 다시 돌아와도 이어져요.",
                placement = CoachHelpPlacement.Bottom
            ),
            CoachHelpStep(
                route = BottomNavItem.Timer.route,
                title = "집중 측정",
                description = "과목의 재생 버튼을 누른 뒤 오른쪽 위 카메라 버튼을 누르면 집중 측정을 시작해요. 카메라 화면을 닫으면 타이머도 멈춰요.",
                placement = CoachHelpPlacement.Top
            ),
            CoachHelpStep(
                route = BottomNavItem.Stats.route,
                title = "Stats 탭",
                description = "마지막으로 Stats 화면에서 공부 기록과 집중 흐름을 확인해요. 기간별 공부량과 코멘트를 보면서 다음 계획을 조정하면 돼요.",
                placement = CoachHelpPlacement.Top
            )
        )
    }
    var showCoachHelp by remember { mutableStateOf(true) }
    var coachHelpIndex by remember { mutableLongStateOf(0L) }
    val currentCoachStep = coachHelpSteps.getOrNull(coachHelpIndex.toInt())

    LaunchedEffect(currentCoachStep?.route) {
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
        isIgnoringBatteryOptimizations,
        requestedBatteryOptimizationThisSession
    ) {
        if (
            settingsViewModel.notificationSettingsLoaded &&
            settingsViewModel.notificationEnabled &&
            !isIgnoringBatteryOptimizations &&
            !requestedBatteryOptimizationThisSession
        ) {
            requestedBatteryOptimizationThisSession = true
            context.openBatteryOptimizationSettings()
        }

        if (
            settingsViewModel.notificationSettingsLoaded &&
            settingsViewModel.notificationEnabled &&
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
            if (event == Lifecycle.Event.ON_RESUME && settingsViewModel.notificationEnabled) {
                isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations()
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
            startDestination = BottomNavItem.ScheduleSetting.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.ScheduleSetting.route) {
                ScheduleSettingScreen()
            }

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
                SettingScreen(
                    navController = navController,
                    onHelpClick = {
                        coachHelpIndex = 0L
                        showCoachHelp = true
                    }
                )
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
        }
    }

    if (showCoachHelp && currentCoachStep != null) {
        CoachHelpOverlay(
            step = currentCoachStep,
            currentStepIndex = coachHelpIndex.toInt(),
            totalStepCount = coachHelpSteps.size,
            onNext = {
                val nextIndex = coachHelpIndex + 1
                if (nextIndex < coachHelpSteps.size) {
                    coachHelpIndex = nextIndex
                } else {
                    showCoachHelp = false
                }
            }
        )
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
