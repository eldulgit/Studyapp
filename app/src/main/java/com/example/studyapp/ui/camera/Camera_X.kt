package com.example.studyapp.ui.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

object CameraXManager {

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        vararg useCases: UseCase,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA, // 전면 카메라 기본
        onConfigured: (ProcessCameraProvider) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases
                )
                onConfigured(cameraProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }
}
