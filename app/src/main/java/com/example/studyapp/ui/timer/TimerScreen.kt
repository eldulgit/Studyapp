package com.example.studyapp.ui.timer

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.studyapp.ui.camera.CameraScreen
import com.example.studyapp.ui.help.CoachHelpTargets
import com.example.studyapp.ui.help.coachHelpTarget
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.timer.pomodoro.CircularTimer
import com.example.studyapp.ui.timer.pomodoro.buildSingleSubjectSegment
import com.example.studyapp.ui.timer.pomodoro.formatCountdown
import com.example.studyapp.ui.timer.pomodoro.formatHoursMinutes
import com.example.studyapp.ui.settings.SettingsViewModel
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme

@Composable
fun TimerScreen(
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val isDarkTheme = isAppInDarkTheme()
    var showCameraHint by remember { mutableStateOf(false) } // 추가 함

    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjectsFromFirestore()
        timerViewModel.loadTodayGeneratedScheduleTimersFromDb()
    }

    DisposableEffect(lifecycleOwner) {
        val owner = lifecycleOwner

        if (owner != null) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        if (timerViewModel.runningTaskId == null) {
                            timerViewModel.loadTodayGeneratedScheduleTimersFromDb()
                        }
                    }

                    Lifecycle.Event.ON_STOP -> {
                        timerViewModel.saveCurrentTimerProgress()
                    }

                    else -> Unit
                }
            }

            owner.lifecycle.addObserver(observer)

            onDispose {
                owner.lifecycle.removeObserver(observer)
            }
        } else {
            onDispose { }
        }
    }

    var showCameraPreview by remember { mutableStateOf(false) }

    /*
     * pauseIconTaskId:
     * - 리스트 아이콘만 Ⅱ 모양으로 보여줄지 결정하는 상태
     *
     * selectedTaskId:
     * - 선택된 과목 유지
     * - 원형 타이머 표시
     * - 카메라 인식 시 resumeByCamera()가 다시 시작할 과목
     *
     * 원하는 동작:
     * 1. 과목 재생 버튼 클릭 -> 아이콘 Ⅱ, 시간은 아직 안 감
     * 2. 카메라 진입 -> 인식되면 시간 감
     * 3. 카메라 종료 -> 시간 멈춤, 선택 과목 유지, 아이콘은 세모
     */
    var pauseIconTaskId by remember { mutableStateOf<Long?>(null) }

    fun showCamera() {
        showCameraPreview = true
        timerViewModel.startCameraMonitoring()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera()
        } else {
            timerViewModel.pause()
            pauseIconTaskId = null
        }
    }

    fun openCamera() {
        if (timerViewModel.selectedTaskId == null || pauseIconTaskId == null) {
            Toast.makeText(
                context,
                "타이머를 먼저 시작해주세요.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        timerViewModel.pauseByCamera()

        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                showCamera()
            }

            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val availableSubjects = subjectViewModel.subjects

    // TODO: DB 연결 완료 후 아래 줄로 교체
    // val timerSubjects = timerViewModel.todayScheduleTimers
    val timerSubjects = timerViewModel.subjects

    var showTimeEditDialog by remember { mutableStateOf(false) }
    var editTargetId by remember { mutableLongStateOf(-1L) }

    val timerWidth = 340.dp

    val currentEditTarget = timerSubjects.firstOrNull { it.id == editTargetId }

    var lastSelectedTaskId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(timerViewModel.selectedTaskId) {
        if (timerViewModel.selectedTaskId != null) {
            lastSelectedTaskId = timerViewModel.selectedTaskId
        } else {
            pauseIconTaskId = null
        }
    }

    val displayTaskId = timerViewModel.selectedTaskId ?: lastSelectedTaskId

    val selectedTask = timerSubjects.firstOrNull { it.id == displayTaskId }

    val segments = if (selectedTask == null) {
        emptyList()
    } else {
        buildSingleSubjectSegment(
            allocatedSeconds = selectedTask.allocatedSeconds,
            remainingSeconds = selectedTask.remainingSeconds,
            taskId = selectedTask.id,
            label = selectedTask.name
        )
    }

    val runningTaskColor = availableSubjects
        .firstOrNull { it.name == selectedTask?.name }
        ?.let { subjectColorForTheme(Color(it.colorArgb), isDarkTheme) }
        ?: selectedTask?.colorArgb?.let { subjectColorForTheme(Color(it), isDarkTheme) }
        ?: MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "집중 타이머",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .coachHelpTarget(CoachHelpTargets.TimerCamera),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        onClick = {
                            showCameraHint = false
                            openCamera()
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(244.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularTimer(
                        modifier = Modifier.fillMaxSize(),
                        segments = segments,
                        colorForIndex = { runningTaskColor }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedTask?.let { formatCountdown(it.remainingSeconds) } ?: "00:00:00",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = selectedTask?.name ?: "과목을 선택해 주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentPadding = PaddingValues(bottom = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = timerSubjects,
                            key = { it.id }
                        ) { item ->
                            val isRunningIcon = pauseIconTaskId == item.id

                            val subjectColorArgb = availableSubjects
                                .firstOrNull { it.name == item.name }
                                ?.colorArgb
                                ?: item.colorArgb

                            val subjectColor = subjectColorArgb
                                ?.let { subjectColorForTheme(Color(it), isDarkTheme) }
                                ?: MaterialTheme.colorScheme.primary

                            TimerTaskRow(
                                subject = item.name,
                                time = formatCountdown(item.remainingSeconds),
                                subjectColorArgb = subjectColor.toArgb(),
                                containerWidth = timerWidth,
                                isRunning = isRunningIcon,
                                onToggle = {
                                    if (pauseIconTaskId == item.id) {
                                    /*
                                 * Ⅱ 아이콘 상태에서 다시 누르면 선택 해제
                                 */
                                        timerViewModel.toggleTask(item.id)
                                        pauseIconTaskId = null
                                        showCameraHint = false
                                    } else {
                                    /*
                                 * 세모 아이콘 상태에서 누르면 선택 상태로 만들고
                                 * 아이콘만 Ⅱ로 변경
                                 *
                                 * 카메라에서 나온 직후에는 selectedTaskId가 이미 같은 과목일 수 있음.
                                 * 이때 toggleTask()를 호출하면 선택이 해제되므로 호출하지 않음.
                                 */
                                        if (timerViewModel.selectedTaskId != item.id) {
                                            timerViewModel.toggleTask(item.id)
                                        }

                                        pauseIconTaskId = item.id
                                        showCameraHint = true
                                    }
                                },
                                onEditClick = {
                                    editTargetId = item.id
                                    showTimeEditDialog = true
                                }
                            )
                        }
                    }
                }
            }
            if (showCameraHint) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = "카메라 버튼을 눌러주세요!",
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            ),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }

    if (showCameraPreview) {
        Dialog(
            onDismissRequest = {
                timerViewModel.stopCameraMonitoring()
                timerViewModel.pauseByCamera()

                showCameraPreview = false

                /*
                 * 카메라에서 나오면:
                 * - 시간은 멈춤
                 * - selectedTaskId는 유지
                 * - 원형 타이머 진행상황/색상 유지
                 * - 아이콘만 세모 모양으로 변경
                 */
                pauseIconTaskId = null
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CameraScreen(
                    timerViewModel = timerViewModel,
                    drowsinessAlertEnabled = settingsViewModel.drowsinessAlertEnabled
                )
            }
        }
    }

    TimeEditDialog(
        show = showTimeEditDialog,
        initialTime = currentEditTarget?.let {
            formatHoursMinutes(it.allocatedSeconds)
        } ?: "0H 0M",
        onDismiss = {
            showTimeEditDialog = false
            editTargetId = -1L
        },
        onSave = { hour, minute ->
            currentEditTarget?.let { subject ->
                timerViewModel.updateSubjectTime(subject.id, hour, minute)
            }
            showTimeEditDialog = false
            editTargetId = -1L
        }
    )

}
