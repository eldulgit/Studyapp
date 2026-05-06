package com.example.studyapp.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors
import kotlin.math.abs // 추가됨

// 공부 집중도 상태 정의 (노란색, 주황색 경고 추가됨)
enum class FocusStatus(val message: String) {
    ACTIVE("열심히 공부 중이시네요! (활동)"),
    INACTIVE_STARE("멍때리는 중이신가요? 집중해 주세요! (비활동)"),
    DROWSY_WARNING("눈이 감기고 있어요! 조심하세요!"), // 노란색 경고
    DROWSY("졸고 계신 것 같아요! 깨어나세요!"),        // 빨간색 정지
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

    fun close() {
        detector.close()
    }

    // [시간 임계값 설정]
// ... rest of the class

    private var absentStartTime: Long = 0L
    private val ABSENT_THRESHOLD_MS = 3000L // 3초

    // 졸음 타이머 적용 (2초 / 3초)
    private var eyesClosedStartTime: Long = 0L
    private val DROWSY_WARNING_THRESHOLD_MS = 2000L // 2초 (노란색)
    private val DROWSY_THRESHOLD_MS = 3000L         // 3초 (빨간색)

    // 멍때림 감지 변수
    private var headStillStartTime: Long = 0L
    private val BLANK_STARE_THRESHOLD_MS = 30000L   // 멍때림: 30초
    private val HEAD_MOVEMENT_THRESHOLD = 2.0f      // 움직임 기준: 2도
    private var anchorHeadX: Float? = null
    private var anchorHeadY: Float? = null

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    val currentTime = System.currentTimeMillis()

                    if (faces.isEmpty()) {
                        // [1. 자리 비움 로직]
                        eyesClosedStartTime = 0L
                        headStillStartTime = 0L

                        if (absentStartTime == 0L) {
                            absentStartTime = currentTime
                            onStatusChanged(FocusStatus.UNKNOWN)
                        } else if (currentTime - absentStartTime >= ABSENT_THRESHOLD_MS) {
                            onStatusChanged(FocusStatus.ABSENT)
                        } else {
                            onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    } else {
                        // [얼굴 인식됨]
                        absentStartTime = 0L
                        val face = faces[0]
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f

                        if (leftEyeOpen < 0.4f && rightEyeOpen < 0.4f) {
                            // [2. 졸음 판단 로직]
                            headStillStartTime = 0L

                            if (eyesClosedStartTime == 0L) {
                                eyesClosedStartTime = currentTime
                                onStatusChanged(FocusStatus.ACTIVE)
                            } else {
                                val duration = currentTime - eyesClosedStartTime
                                // 시간이 지남에 따라 상태가 바뀜
                                when {
                                    duration >= DROWSY_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY)
                                    duration >= DROWSY_WARNING_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY_WARNING)
                                    else -> onStatusChanged(FocusStatus.ACTIVE)
                                }
                            }
                        } else {
                            // [3. 집중 및 멍때림 판단 로직]
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
                                } else {
                                    onStatusChanged(FocusStatus.ACTIVE)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e -> e.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}

fun createFocusAnalyzer(analyzer: ImageAnalysis.Analyzer): ImageAnalysis {
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(
        Executors.newSingleThreadExecutor(),
        analyzer
    )
    return imageAnalysis
}