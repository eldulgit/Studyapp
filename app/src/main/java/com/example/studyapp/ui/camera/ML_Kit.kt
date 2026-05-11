package com.example.studyapp.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
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
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    private val ABSENT_THRESHOLD_MS = 3000L
    private val DROWSY_WARNING_THRESHOLD_MS = 2000L
    private val DROWSY_THRESHOLD_MS = 3000L
    private val BLANK_STARE_THRESHOLD_MS = 10000L
    private val HEAD_MOVEMENT_THRESHOLD = 2.0f
    private val HEAD_DOWN_THRESHOLD = 12.0f

    // ★ 빡센 각도 기준 추가 (좌우 25도, 기울기 25도)
    private val STRICT_ANGLE_THRESHOLD = 25.0f

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

        val frameWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val frameHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        detector.process(image)
            .addOnSuccessListener { faces ->
                val currentTime = System.currentTimeMillis()

                if (faces.isEmpty()) {
                    val isNearEdge = lastFaceX < 0.3f || lastFaceX > 0.7f || lastFaceY < 0.3f || lastFaceY > 0.7f

                    if (!isNearEdge) {
                        absentStartTime = 0L
                        if (eyesClosedStartTime == 0L) eyesClosedStartTime = currentTime
                        val duration = currentTime - eyesClosedStartTime
                        when {
                            duration >= DROWSY_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY)
                            duration >= DROWSY_WARNING_THRESHOLD_MS -> onStatusChanged(FocusStatus.DROWSY_WARNING)
                            else -> onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    } else {
                        eyesClosedStartTime = 0L
                        if (absentStartTime == 0L) absentStartTime = currentTime
                        if (currentTime - absentStartTime >= ABSENT_THRESHOLD_MS) {
                            onStatusChanged(FocusStatus.ABSENT)
                        } else {
                            onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    }
                } else {
                    val face = faces[0]

                    // 1. 얼굴 크기 검사 (화면의 가로세로 비율 대비 너무 작으면 노이즈로 간주)
                    val faceWidthRatio = face.boundingBox.width().toFloat() / frameWidth
                    val faceHeightRatio = face.boundingBox.height().toFloat() / frameHeight
                    val isTooSmall = faceWidthRatio < 0.1f || faceHeightRatio < 0.1f // 화면의 10%보다 작으면 아웃

                    // 2. 이목구비 랜드마크 추출
                    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                    val nose = face.getLandmark(FaceLandmark.NOSE_BASE)
                    val mouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)

                    // 3. 눈 깜빡임 확률 유무 체크 (뒤통수일 땐 확률을 못 구해서 null이 됨)
                    val leftEyeProb = face.leftEyeOpenProbability
                    val rightEyeProb = face.rightEyeOpenProbability
                    val isEyesNotVisible = leftEyeProb == null || rightEyeProb == null

                    // 4. 고개 각도 타이트하게 체크 (좌우 Y축, 갸우뚱 Z축)
                    val yaw = abs(face.headEulerAngleY)
                    val roll = abs(face.headEulerAngleZ)
                    val isHeadTurnedAway = yaw > STRICT_ANGLE_THRESHOLD || roll > STRICT_ANGLE_THRESHOLD

                    // ★ 핵심 로직: 하나라도 걸리면 무조건 자리 비움(딴짓)으로 간주
                    if (isTooSmall || leftEye == null || rightEye == null || nose == null || mouth == null || isEyesNotVisible || isHeadTurnedAway) {
                        eyesClosedStartTime = 0L // 졸음 타이머 초기화

                        if (absentStartTime == 0L) absentStartTime = currentTime
                        if (currentTime - absentStartTime >= ABSENT_THRESHOLD_MS) {
                            onStatusChanged(FocusStatus.ABSENT)
                        } else {
                            // 아직 3초가 안 지났으면 유예 기간
                            onStatusChanged(FocusStatus.UNKNOWN)
                        }
                    } else {
                        // --- 아주 정직하게 정면을 보고 눈코입이 다 보이는 상태 ---
                        absentStartTime = 0L

                        lastFaceX = face.boundingBox.centerX().toFloat() / frameWidth
                        lastFaceY = face.boundingBox.centerY().toFloat() / frameHeight
                        lastHeadTiltX = face.headEulerAngleX // 고개 숙임 정도 (Pitch)

                        val leftEyeOpen = leftEyeProb ?: 1.0f
                        val rightEyeOpen = rightEyeProb ?: 1.0f

                        // 졸음 감지 체크 (고개를 12도 이상 숙이거나 눈을 감았을 때)
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