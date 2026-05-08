package com.example.studyapp.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors
import kotlin.math.abs

enum class FocusStatus(val message: String) {
    ACTIVE("열심히 공부 중이시네요! (활동)"),
    INACTIVE_STARE("멍때리는 중이신가요? 집중해 주세요! (비활동)"),
    DROWSY_WARNING("눈이 감기고 있어요! 조심하세요!"),
    DROWSY("졸고 계신 것 같아요! 깨어나세요!"),
    ABSENT("자리를 비우셨나요? 공부를 시작해 주세요."),
    UNKNOWN("얼굴을 찾는 중...")
}
class MLKitFocusAnalyzer(
    private val onStatusChanged: (FocusStatus) -> Unit
) : ImageAnalysis.Analyzer {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    private val detector = FaceDetection.getClient(options)
    private val ABSENT_THRESHOLD_MS = 3000L
    private val DROWSY_WARNING_THRESHOLD_MS = 2000L
    private val DROWSY_THRESHOLD_MS = 3000L
    private val BLANK_STARE_THRESHOLD_MS = 30000L
    private val HEAD_MOVEMENT_THRESHOLD = 2.0f
    private val HEAD_DOWN_THRESHOLD = 12.0f
    private var absentStartTime: Long = 0L
    private var eyesClosedStartTime: Long = 0L
    private var headStillStartTime: Long = 0L
    private var anchorHeadX: Float? = null
    private var anchorHeadY: Float? = null
    private var lastFaceX: Float = 0.5f
    private var lastFaceY: Float = 0.5f
    private var lastHeadTiltX: Float = 0f

    fun close() {
        detector.close()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        // 화면 크기 계산
        val frameWidth =
            if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val frameHeight =
            if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        detector.process(image)
            .addOnSuccessListener { faces ->
                val currentTime = System.currentTimeMillis()

                if (faces.isEmpty()) {
                    // 얼굴 인식 안 됨
                    if (absentStartTime == 0L) absentStartTime = currentTime

                    // 마지막 위치가 화면 끝(가장자리 15%)이었는지 체크
                    val isNearEdge =
                        lastFaceX < 0.15f || lastFaceX > 0.85f || lastFaceY < 0.15f || lastFaceY > 0.85f

                    if (!isNearEdge) {
                        // 중앙에서 사라지면 무조건 졸음으로 간주
                        if (eyesClosedStartTime == 0L) eyesClosedStartTime = currentTime
                        val duration = currentTime - eyesClosedStartTime
                        when {
                            duration >= DROWSY_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY)
                            duration >= DROWSY_WARNING_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY_WARNING)
                            // 2초 미만일 때는 '찾는 중' 상태 유지
                            else -> onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    } else {
                        // 가장자리에서 사라지면 자리 비움으로 경고
                        eyesClosedStartTime = 0L
                        if (currentTime - absentStartTime >= ABSENT_THRESHOLD_MS) {
                            onStatusChanged(FocusStatus.ABSENT)
                        } else {
                            onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    }
                } else {
                    // 얼굴 인식 됨
                    absentStartTime = 0L
                    val face = faces[0]

                    // 마지막 위치 데이터 갱신
                    lastFaceX = face.boundingBox.centerX().toFloat() / frameWidth
                    lastFaceY = face.boundingBox.centerY().toFloat() / frameHeight
                    lastHeadTiltX = face.headEulerAngleX

                    val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                    val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f

                    // 졸음 감지 체크
                    if ((leftEyeOpen < 0.4f && rightEyeOpen < 0.4f) || lastHeadTiltX > HEAD_DOWN_THRESHOLD) {
                        if (eyesClosedStartTime == 0L) eyesClosedStartTime = currentTime
                        val duration = currentTime - eyesClosedStartTime
                        when {
                            duration >= DROWSY_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY)
                            duration >= DROWSY_WARNING_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY_WARNING)
                            else -> onStatusChanged(FocusStatus.ACTIVE)
                        }
                    } else {
                        // 정상 및 멍때림 감지 체크
                        eyesClosedStartTime = 0L
                        val currentX = face.headEulerAngleX
                        val currentY = face.headEulerAngleY

                        if (anchorHeadX == null || anchorHeadY == null) {
                            anchorHeadX = currentX
                            anchorHeadY = currentY
                        }

                        val deltaX = abs(currentX - (anchorHeadX ?: 0f))
                        val deltaY = abs(currentY - (anchorHeadY ?: 0f))

                        if (deltaX > HEAD_MOVEMENT_THRESHOLD || deltaY > HEAD_MOVEMENT_THRESHOLD) {
                            headStillStartTime = 0L
                            anchorHeadX = currentX
                            anchorHeadY = currentY
                            onStatusChanged(FocusStatus.ACTIVE)
                        } else {
                            if (headStillStartTime == 0L) {
                                headStillStartTime = currentTime
                                onStatusChanged(FocusStatus.ACTIVE)
                            } else if (currentTime - headStillStartTime >= BLANK_STARE_THRESHOLD_MS) {
                                onStatusChanged(FocusStatus.INACTIVE_STARE)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { imageProxy.close() }
    }
}
fun createFocusAnalyzer(analyzer: ImageAnalysis.Analyzer): ImageAnalysis {
    return ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
        setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
    }
}
