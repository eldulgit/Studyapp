package com.example.studyapp.ui.timer.pomodoro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CircularTimer(
    modifier: Modifier = Modifier,
    segments: List<CircularSegmentUi> = emptyList(),
    colorForIndex: (Int) -> Color = { Color(0xFF4CAF50) }
) {
    val rawRemainingSweep = segments.firstOrNull()?.sweepAngle?.coerceIn(0f, 360f) ?: 0f
    val animatedSweep = remember { Animatable(rawRemainingSweep) }
    val startAngle = segments.firstOrNull()?.startAngle ?: -90f

    LaunchedEffect(rawRemainingSweep) {
        if (animatedSweep.value == 0f && rawRemainingSweep > 0f) {
            animatedSweep.snapTo(rawRemainingSweep)
        } else {
            animatedSweep.animateTo(
                targetValue = rawRemainingSweep,
                animationSpec = tween(
                    durationMillis = 950,
                    easing = LinearEasing
                )
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 28.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        val baseGray = Color(0xFFD9D4DC)

        drawArc(
            color = baseGray,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Butt
            )
        )

        if (animatedSweep.value > 0f) {
            drawArc(
                color = colorForIndex(0),
                startAngle = startAngle,
                sweepAngle = animatedSweep.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Butt
                )
            )
        }
    }
}