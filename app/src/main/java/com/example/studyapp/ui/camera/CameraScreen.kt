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

            PersonGuideOverlay(
                modifier = Modifier.fillMaxSize()
            )

            // 집중도 상태 오버레이
            FocusOverlay(
                status = focusStatus,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

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
            text = "가이드 안에 얼굴과 상반신을 맞춰주세요",
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

            val headWidth = size.width * 0.22f
            val headHeight = headWidth * 1.25f
            val headTop = size.height * 0.20f
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
                style = Stroke(
                    width = strokeWidth
                )
            )

            val shoulderY = headTop + headHeight + size.height * 0.08f
            val bodyBottomY = size.height * 0.76f

            val shoulderWidth = size.width * 0.62f
            val waistWidth = size.width * 0.34f

            val bodyPath = Path().apply {
                moveTo(
                    x = centerX - shoulderWidth / 2f,
                    y = shoulderY
                )

                cubicTo(
                    x1 = centerX - shoulderWidth * 0.42f,
                    y1 = shoulderY + size.height * 0.06f,
                    x2 = centerX - waistWidth / 2f,
                    y2 = bodyBottomY - size.height * 0.18f,
                    x3 = centerX - waistWidth / 2f,
                    y3 = bodyBottomY
                )

                lineTo(
                    x = centerX + waistWidth / 2f,
                    y = bodyBottomY
                )

                cubicTo(
                    x1 = centerX + waistWidth / 2f,
                    y1 = bodyBottomY - size.height * 0.18f,
                    x2 = centerX + shoulderWidth * 0.42f,
                    y2 = shoulderY + size.height * 0.06f,
                    x3 = centerX + shoulderWidth / 2f,
                    y3 = shoulderY
                )
            }

            drawPath(
                path = bodyPath,
                color = guideColor,
                style = Stroke(
                    width = strokeWidth
                )
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = size.width * 0.36f,
                center = Offset(
                    x = centerX,
                    y = size.height * 0.48f
                ),
                style = Stroke(
                    width = 2.dp.toPx()
                )
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
