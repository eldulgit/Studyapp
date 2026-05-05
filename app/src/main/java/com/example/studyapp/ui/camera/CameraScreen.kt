package com.example.studyapp.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.studyapp.ui.timer.TimerViewModel

@Composable
fun CameraScreen(timerViewModel: TimerViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 현재 집중도 상태 관리
    var focusStatus by remember { mutableStateOf(FocusStatus.UNKNOWN) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // 카메라 제공자 관리를 위한 상태
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    // 화면을 벗어날 때 카메라 해제
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    // 🌟 [수정 완료] 새로운 상태(노란색, 멍때림)에 맞춘 타이머 제어
    LaunchedEffect(focusStatus) {
        when (focusStatus) {
            // 타이머 정지: 얼굴 찾는 중, 자리 비움, 최종 졸음
            FocusStatus.UNKNOWN,
            FocusStatus.ABSENT,
            FocusStatus.DROWSY -> {
                timerViewModel.pauseByCamera()
            }

            // 타이머 재개: 정상 활동, 멍때림 경고, 졸음 경고
            FocusStatus.ACTIVE,
            FocusStatus.INACTIVE_STARE,
            FocusStatus.DROWSY_WARNING -> {
                timerViewModel.resumeByCamera()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val focusAnalysis = createFocusAnalyzer { status: FocusStatus ->
                        focusStatus = status
                    }

                    CameraXManager.startCamera(
                        ctx,
                        lifecycleOwner,
                        preview,
                        focusAnalysis,
                        { provider ->
                            cameraProvider = provider
                        }
                    )

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

//            PersonGuideOverlay(
//                modifier = Modifier.fillMaxSize()
//            )

            // 집중도 상태 오버레이
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CameraTimerOverlay(
                    timerViewModel = timerViewModel
                )

                Spacer(modifier = Modifier.height(8.dp))

                FocusOverlay(
                    status = focusStatus
                )
            }

        } else {
            PermissionDeniedMessage()
        }
    }
}
// 새로운 상태들에 맞춘 색상 및 문구 처리
@Composable
fun FocusOverlay(status: FocusStatus, modifier: Modifier = Modifier) {
    val backgroundColor = when (status) {
        FocusStatus.ACTIVE -> Color.Black.copy(alpha = 0.6f)
        FocusStatus.INACTIVE_STARE -> Color(0xFFFFA500).copy(alpha = 0.7f) // 주황색
        FocusStatus.DROWSY_WARNING -> Color.Yellow.copy(alpha = 0.8f)      // 노란색
        FocusStatus.DROWSY -> Color.Red.copy(alpha = 0.7f)                 // 빨간색
        FocusStatus.ABSENT -> Color(0xFF1E90FF).copy(alpha = 0.7f)         // 파란색
        FocusStatus.UNKNOWN -> Color.Gray.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = status.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status.message,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

//카메라 권한
@Composable
fun PermissionDeniedMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "카메라 권한이 필요합니다.", color = Color.Gray)
    }
}

@Composable
fun CameraTimerOverlay(
    timerViewModel: TimerViewModel
) {
    val targetTaskId = timerViewModel.runningTaskId ?: timerViewModel.selectedTaskId

    val currentSubject = timerViewModel.subjects.firstOrNull {
        it.id == targetTaskId
    }

    val subjectName = currentSubject?.name ?: "선택된 과목 없음"
    val remainingSeconds = currentSubject?.remainingSeconds ?: 0

    Column(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = subjectName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "남은 시간 ${formatCameraTimerTime(remainingSeconds)}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatCameraTimerTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}