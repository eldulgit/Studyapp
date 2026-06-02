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
    val shouldShowCoachHelpOnFirstRun = true
    val coachHelpSteps = remember {
        listOf(
            CoachHelpStep(
                route = BottomNavItem.ScheduleSetting.route,
                title = "스케줄과 목표 추가",
                description = "Setup 화면의 + 버튼을 누르면 추가 화면이 열려요. 여기서 고정 스케줄이나 목표를 추가할 수 있고, 목표는 기간을 입력한 뒤 체크를 켜면 마감일이 가까워질수록 우선순위가 자동으로 올라가요.",
                placement = CoachHelpPlacement.Bottom,
                highlight = circleHighlight(0.90f, 0.84f, 0.15f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Setting.route,
                title = "과목 설정",
                description = "Settings에서 과목 설정으로 들어가 과목을 등록해요. 과목은 스케줄링, 타이머, 통계에서 같은 이름과 색상으로 연결돼요.",
                placement = CoachHelpPlacement.Center,
                highlight = rectHighlight(0.15f, 0.275f, 0.25f, 0.048f)
            ),
            CoachHelpStep(
                route = "setting_subject",
                title = "과목 저장",
                description = "과목명을 입력하고 중요도를 선택한 뒤 과목 색상을 고르세요. 오른쪽 위 저장 버튼을 누르면 과목이 저장되고 아래쪽에 카드로 표시돼요.",
                placement = CoachHelpPlacement.Top,
                highlight = circleHighlight(0.91f, 0.065f, 0.085f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Calendar.route,
                title = "스케줄링",
                description = "Schedule 화면에서 달력 옆 스케줄링 아이콘을 누르면 과목 라벨과 시간표가 자동으로 생성돼요. 만들어진 스케줄에 맞춰 공부하면 됩니다.",
                placement = CoachHelpPlacement.Top,
                highlight = circleHighlight(0.84f, 0.065f, 0.072f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Timer.route,
                title = "카메라 버튼",
                description = "스케줄링 후 Timer로 넘어가면 과목별 시간이 자동으로 들어와요. 재생 버튼으로 과목을 선택하고, 수정 버튼으로 시간을 바꿀 수 있어요. 카메라 아이콘을 누르면 카메라 인식으로 집중 측정을 시작합니다.",
                placement = CoachHelpPlacement.Top,
                highlight = circleHighlight(0.89f, 0.07f, 0.085f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Stats.route,
                title = "Stats 필터",
                description = "Stats 화면 위쪽 라벨로 누적 공부시간의 기간을 선택해요. Daily, Weekly, Monthly를 눌러 일간, 주간, 월간 누적 시간을 확인할 수 있어요.",
                placement = CoachHelpPlacement.Top,
                highlight = rectHighlight(0.50f, 0.09f, 0.92f, 0.045f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Stats.route,
                title = "누적 공부시간 그래프",
                description = "누적 공부시간 그래프는 선택한 기간에 맞춰 공부 시간이 얼마나 쌓였는지 보여줘요. 막대 위 시간 라벨로 공부량을 빠르게 확인할 수 있어요.",
                placement = CoachHelpPlacement.Center,
                highlight = rectHighlight(0.50f, 0.302f, 0.86f, 0.235f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Stats.route,
                title = "시간대별 집중도",
                description = "시간대별 집중도 그래프는 카메라 인식에서 집중 상태로 판단된 시간이 어느 시간대에 많이 쌓였는지 보여줘요. 집중이 잘 되는 시간을 찾는 데 사용할 수 있어요.",
                placement = CoachHelpPlacement.Center,
                highlight = rectHighlight(0.50f, 0.635f, 0.90f, 0.31f)
            ),
            CoachHelpStep(
                route = BottomNavItem.Setting.route,
                title = "도움말 다시 보기",
                description = "나중에 사용법이 다시 필요하면 Settings의 도움말을 눌러주세요. 키워드 검색과 자세한 가이드로 기능을 다시 확인할 수 있어요.",
                placement = CoachHelpPlacement.Center,
                highlight = rectHighlight(0.12f, 0.62f, 0.22f, 0.05f)
            )
        )
    }
    var showCoachHelp by remember { mutableStateOf(shouldShowCoachHelpOnFirstRun) }
    var showCoachHelpOverlay by remember { mutableStateOf(false) }
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
            onNext = {
                val nextIndex = coachHelpIndex + 1
                showCoachHelpOverlay = false
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
