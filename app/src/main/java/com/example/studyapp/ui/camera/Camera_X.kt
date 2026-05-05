package com.example.studyapp.ui.camera

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

object CameraXManager {
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        imageAnalysis: ImageAnalysis,
        onConfigured: (ProcessCameraProvider) -> Unit
    ) {
        // Activity 컨텍스트를 우선적으로 찾아서 사용 (AppOps 권한 연결 문제 해결용)
        val activityContext = context.findActivity() ?: context
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activityContext)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // lifecycleOwner가 유효한 상태인지 확인
                if (lifecycleOwner.lifecycle.currentState == androidx.lifecycle.Lifecycle.State.DESTROYED) {
                    android.util.Log.w("CameraXManager", "Lifecycle is destroyed, skipping binding.")
                    return@addListener
                }

                //전면 카메라(셀카 방향) 사용
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                // 해당 카메라가 존재하는지 확인
                if (!cameraProvider.hasCamera(cameraSelector)) {
                    android.util.Log.e("CameraXManager", "Front camera not found")
                    return@addListener
                }

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
                android.util.Log.e("CameraXManager", "Use case binding failed", e)
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(activityContext))
    }
}
