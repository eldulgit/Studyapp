package com.example.studyapp.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

class MLKitTextAnalyzer(
    private val onTextDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    onTextDetected(visionText.text)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener
                {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
fun getMLKitAnalyzer(onTextDetected: (String) -> Unit): ImageAnalysis {
    val analyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    analyzer.setAnalyzer(Executors.newSingleThreadExecutor(), MLKitTextAnalyzer(onTextDetected))
    return analyzer
}
