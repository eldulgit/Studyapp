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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.studyapp.ui.camera.CameraScreen
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.timer.pomodoro.CircularTimer
import com.example.studyapp.ui.timer.pomodoro.buildSingleSubjectSegment
import com.example.studyapp.ui.timer.pomodoro.formatCountdown
import com.example.studyapp.ui.timer.pomodoro.formatHoursMinutes
import com.example.studyapp.ui.settings.SettingsViewModel

@Composable
fun TimerScreen(
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel
) {
    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjectsFromFirestore()
        timerViewModel.loadTodayGeneratedScheduleTimersFromDb()
    }

    val context = LocalContext.current

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

    var showSubjectDialog by remember { mutableStateOf(false) }
    var checkedSubjects by remember { mutableStateOf(setOf<String>()) }

    var showTimeEditDialog by remember { mutableStateOf(false) }
    var editTargetId by remember { mutableLongStateOf(-1L) }

    val timerWidth = 270.dp

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
        ?.let { Color(it.colorArgb) }
        ?: Color(0xFF4CAF50)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubjectDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "과목 추가"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 0.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { openCamera() }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.size(270.dp)) {
                CircularTimer(
                    modifier = Modifier.fillMaxSize(),
                    segments = segments,
                    colorForIndex = { runningTaskColor }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .width(timerWidth)
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                items(
                    items = timerSubjects,
                    key = { it.id }
                ) { item ->
                    val isRunningIcon = pauseIconTaskId == item.id

                    val subjectColorArgb = availableSubjects
                        .firstOrNull { it.name == item.name }
                        ?.colorArgb
                        ?: Color.Gray.toArgb()

                    TimerTaskRow(
                        subject = item.name,
                        time = formatCountdown(item.remainingSeconds),
                        subjectColorArgb = subjectColorArgb,
                        containerWidth = timerWidth,
                        isRunning = isRunningIcon,
                        onToggle = {
                            if (pauseIconTaskId == item.id) {
                                /*
                                 * Ⅱ 아이콘 상태에서 다시 누르면 선택 해제
                                 */
                                timerViewModel.toggleTask(item.id)
                                pauseIconTaskId = null
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

    SubjectSelectDialog(
        show = showSubjectDialog,
        availableSubjects = availableSubjects,
        checkedSubjects = checkedSubjects,
        onCheckedChange = { subjectName, checked ->
            checkedSubjects =
                if (checked) checkedSubjects + subjectName
                else checkedSubjects - subjectName
        },
        onDismiss = {
            checkedSubjects = emptySet()
            showSubjectDialog = false
        },
        onConfirm = {
            checkedSubjects.forEach { subjectName ->
                timerViewModel.addSubjectTimer(subjectName)
            }
            checkedSubjects = emptySet()
            showSubjectDialog = false
        }
    )
}
