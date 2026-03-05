package com.example.studyapp.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CircularTimer(
    modifier: Modifier = Modifier,
    sweepAngle: Float = 120f // UI용 임시 값
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val strokeWidth = 20.dp.toPx()
        val startAngle = -90f
        val angleInRad = Math.toRadians((startAngle + sweepAngle).toDouble())

        val radius = size.minDimension / 2
        val center = center

        val dotX = center.x + radius * kotlin.math.cos(angleInRad).toFloat()
        val dotY = center.y + radius * kotlin.math.sin(angleInRad).toFloat()

        // 끝 점
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 8.dp.toPx(),
            center = Offset(dotX, dotY)
        )

        // 배경 원
        drawArc(
            color = Color.LightGray,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // 진행 호
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}
