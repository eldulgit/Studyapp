package com.example.studyapp.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

// 공부 집중도 상태 정의
enum class FocusStatus(val message: String) {
    FOCUSING("집중하고 있습니다."),
    DROWSY("졸고 계신 것 같아요! 깨어나세요!"),
    ABSENT("자리를 비우셨나요? 공부를 시작해 주세요."),
    UNKNOWN("얼굴을 찾는 중...")
    /*사용자의 상태를 4가지(FOCUSING, DROWSY, ABSENT, UNKNOWN)로 나누고,
    각 상태에 맞는 안내 메시지를 저장해 두는 열거형(Enum) 클래스*/
}

class MLKitFocusAnalyzer(
    private val onStatusChanged: (FocusStatus) -> Unit
) : ImageAnalysis.Analyzer {

    // 얼굴 인식 옵션: 눈 뜨기 확률 및 실시간 모드 설정
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    //ML Kit의 얼굴 인식 옵션을 설정.
    //PERFORMANCE_MODE_FAST: 정확도보다는 빠른 속도(실시간 처리)를 우선시.
    //CLASSIFICATION_MODE_ALL: 얼굴의 윤곽뿐만 아니라 '눈을 떴는지, 감았는지'
    //같은 추가 표정 분류 정보까지 분석하도록 활성화.

    private val detector = FaceDetection.getClient(options)
    //위에서 설정한 옵션을 바탕으로 실제 얼굴 인식기(detector) 객체를 생성.

    // 졸음 판정을 위한 시간 체크 변수
    private var eyesClosedStartTime: Long = 0L
    private val DROWSY_THRESHOLD_MS = 5000L // 5초
    //eyesClosedStartTime에 기록하고, 눈을 감은 상태가 5초 이상
    //지속되면 졸음으로 판단한다.

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        eyesClosedStartTime = 0L // 자리 비움 시 초기화
                        onStatusChanged(FocusStatus.ABSENT)
                    } else {
                        val face = faces[0]
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f
                        
                        // 양쪽 눈 뜨기 확률이 0.4 이하일 때
                        if (leftEyeOpen < 0.4f && rightEyeOpen < 0.4f) {
                            val currentTime = System.currentTimeMillis()
                            if (eyesClosedStartTime == 0L) {
                                // 처음 눈을 감기 시작한 시점 기록
                                eyesClosedStartTime = currentTime
                                onStatusChanged(FocusStatus.FOCUSING) // 아직은 집중 상태로 유지
                            } else {
                                // 5초 이상 지속되었는지 확인
                                if (currentTime - eyesClosedStartTime >= DROWSY_THRESHOLD_MS) {
                                    onStatusChanged(FocusStatus.DROWSY)
                                } else {
                                    onStatusChanged(FocusStatus.FOCUSING)
                                }
                            }
                        } else {
                            // 눈을 다시 떴을 때 시간 초기화
                            eyesClosedStartTime = 0L
                            onStatusChanged(FocusStatus.FOCUSING)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

fun createFocusAnalyzer(onStatusChanged: (FocusStatus) -> Unit): ImageAnalysis {
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(
        Executors.newSingleThreadExecutor(),
        MLKitFocusAnalyzer(onStatusChanged)
    )
    return imageAnalysis
}
