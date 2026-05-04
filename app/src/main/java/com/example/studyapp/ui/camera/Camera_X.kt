package com.example.studyapp.ui.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

object CameraXManager {
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        imageAnalysis: ImageAnalysis,
        onConfigured: (ProcessCameraProvider) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            //전면 카메라(셀카 방향) 사용
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // 기존에 켜져 있던 카메라가 있다면 끄기
                cameraProvider.unbindAll()

                // 화면과 분석기를 카메라에 연결
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // 설정 완료된 provider 넘겨주기
                onConfigured(cameraProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
