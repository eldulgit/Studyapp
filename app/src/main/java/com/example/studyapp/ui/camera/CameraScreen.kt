package com.example.studyapp.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.studyapp.ui.timer.TimerViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

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

    // 집중도 상태 변화에 따른 타이머 제어
    LaunchedEffect(focusStatus) {
        when (focusStatus) {
            FocusStatus.ABSENT -> {
                timerViewModel.pauseByCamera()
            }

            FocusStatus.FOCUSING,
            FocusStatus.DROWSY -> {
                timerViewModel.resumeByCamera()
            }

            FocusStatus.UNKNOWN -> {
                // 얼굴을 찾는 중인 상태입니다.
                // 바로 멈추지 않고 ML_Kit.kt의 3초 기준을 기다립니다.
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

                    // 집중도 분석기 생성 및 상태 업데이트 콜백 연결
                    val focusAnalysis = createFocusAnalyzer { status ->
                        focusStatus = status
                    }

                    // 전면 카메라를 사용
                    CameraXManager.startCamera(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        preview,
                        focusAnalysis,
                        onConfigured = { provider ->
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

@Composable
fun FocusOverlay(status: FocusStatus, modifier: Modifier = Modifier) {
    val backgroundColor = when (status) {
        FocusStatus.FOCUSING -> Color.Black.copy(alpha = 0.6f)
        FocusStatus.DROWSY -> Color.Red.copy(alpha = 0.7f)
        FocusStatus.ABSENT -> Color.Yellow.copy(alpha = 0.7f)
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

//가이드라인
@Composable
fun PersonGuideOverlay(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Text(
            text = "가이드 안에 얼굴과 어깨를 맞춰주세요",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val guideColor = Color.White.copy(alpha = 0.85f)
            val strokeWidth = 5.dp.toPx()

            val centerX = size.width / 2f

            // 머리 가이드
            val headWidth = size.width * 0.24f
            val headHeight = headWidth * 1.22f
            val headTop = size.height * 0.22f
            val headLeft = centerX - headWidth / 2f

            drawOval(
                color = guideColor,
                topLeft = Offset(
                    x = headLeft,
                    y = headTop
                ),
                size = Size(
                    width = headWidth,
                    height = headHeight
                ),
                style = Stroke(width = strokeWidth)
            )

            // 어깨 가이드
            val shoulderTop = headTop + headHeight + size.height * 0.05f
            val shoulderWidth = size.width * 0.58f
            val shoulderHeight = size.height * 0.12f
            val shoulderLeft = centerX - shoulderWidth / 2f

            val shoulderPath = Path().apply {
                moveTo(shoulderLeft, shoulderTop + shoulderHeight)

                cubicTo(
                    shoulderLeft,
                    shoulderTop + shoulderHeight * 0.25f,
                    centerX - shoulderWidth * 0.2f,
                    shoulderTop,
                    centerX,
                    shoulderTop
                )

                cubicTo(
                    centerX + shoulderWidth * 0.2f,
                    shoulderTop,
                    shoulderLeft + shoulderWidth,
                    shoulderTop + shoulderHeight * 0.25f,
                    shoulderLeft + shoulderWidth,
                    shoulderTop + shoulderHeight
                )
            }

            drawPath(
                path = shoulderPath,
                color = guideColor,
                style = Stroke(width = strokeWidth)
            )

            // 얼굴 위치 참고용 약한 원형 가이드
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = size.width * 0.24f,
                center = Offset(
                    x = centerX,
                    y = headTop + headHeight * 0.9f
                ),
                style = Stroke(width = 2.dp.toPx())
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