package com.example.studyapp.ui.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor


 // CameraX 설정을 도와주는 유틸리티 객체

object CameraXManager {

    /**
     * 카메라 미리보기를 시작하는 함수
     * @param context 안드로이드 컨텍스트
     * @param lifecycleOwner 수명 주기 관리자 (Activity 또는 Fragment)
     * @param previewView 카메라 화면을 보여줄 뷰
     * @param onCameraConfigured 카메라 설정이 완료된 후 추가 작업(예: ML Kit 연결)을 위한 콜백
     */
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraConfigured: (ProcessCameraProvider, Preview) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            //유스 케이스 설정에 대해 좀 더 알아보기
            // 미리보기(Preview) 유스케이스 설정
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 뒷면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 기존 바인딩 해제 후 다시 연결
                cameraProvider.unbindAll()
                //바인딩에 대해 좀 더 알아보고 수정해야함.
                // 설정 완료 콜백 호출 (여기서 ML Kit 등을 추가로 바인딩할 수 있음)
                onCameraConfigured(cameraProvider, preview)

                // 기본 미리보기 바인딩
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }
}
