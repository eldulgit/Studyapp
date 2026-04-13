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
            FocusStatus.DROWSY, FocusStatus.ABSENT -> {
                // 졸거나 자리를 비웠을 때 타이머가 실행 중이면 일시정지
                if (timerViewModel.runningTaskId != null) {
                    timerViewModel.pause()
                }
            }
            FocusStatus.FOCUSING -> {
                // 다시 집중할 때 자동으로 시작하게 하고 싶다면 여기에 로직 추가 가능
                // 현재는 명시적으로 멈추는 기능에 집중
            }
            else -> {}
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
//카메라 권한
@Composable
fun PermissionDeniedMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "카메라 권한이 필요합니다.", color = Color.Gray)
    }
}
