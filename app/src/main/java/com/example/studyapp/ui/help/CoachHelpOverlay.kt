package com.example.studyapp.ui.help

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CoachHelpStep(
    val route: String,
    val title: String,
    val description: String,
    val placement: CoachHelpPlacement,
    val highlight: CoachHelpHighlight
)

data class CoachHelpHighlight(
    val shape: CoachHelpHighlightShape,
    val centerXRatio: Float,
    val centerYRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float
)

enum class CoachHelpHighlightShape {
    Circle,
    RoundRect
}

enum class CoachHelpPlacement {
    Top,
    Center,
    Bottom
}

@Composable
fun CoachHelpOverlay(
    step: CoachHelpStep,
    currentStepIndex: Int,
    totalStepCount: Int,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onNext),
    ) {
        SpotlightScrim(
            highlight = step.highlight,
            modifier = Modifier.matchParentSize()
        )

        if (step.description.isNotBlank()) {
            val textCardAlignment = textCardAlignment(step)
            CoachHelpTextCard(
                step = step,
                modifier = Modifier
                    .align(textCardAlignment)
                    .textCardPadding(textCardAlignment)
            )
        }
    }
}

@Composable
private fun CoachHelpTextCard(
    step: CoachHelpStep,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = step.description,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun textCardAlignment(step: CoachHelpStep): Alignment {
    return if (
        step.placement == CoachHelpPlacement.Bottom ||
        step.highlight.centerYRatio > 0.78f
    ) {
        Alignment.TopCenter
    } else {
        Alignment.BottomCenter
    }
}

private fun Modifier.textCardPadding(alignment: Alignment): Modifier {
    return if (alignment == Alignment.TopCenter) {
        padding(horizontal = 18.dp, vertical = 28.dp)
    } else {
        padding(start = 18.dp, end = 18.dp, bottom = 92.dp)
    }
}

@Composable
private fun SpotlightScrim(
    highlight: CoachHelpHighlight,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    ) {
        val highlightPadding = 10.dp.toPx()
        val highlightWidth = size.width * highlight.widthRatio + highlightPadding * 2f
        val highlightHeight = size.height * highlight.heightRatio + highlightPadding * 2f
        val center = Offset(
            x = size.width * highlight.centerXRatio,
            y = size.height * highlight.centerYRatio
        )
        val topLeft = Offset(
            x = center.x - highlightWidth / 2f,
            y = center.y - highlightHeight / 2f
        )

        drawRect(Color.Black.copy(alpha = 0.58f))

        when (highlight.shape) {
            CoachHelpHighlightShape.Circle -> {
                val radius = minOf(highlightWidth, highlightHeight) / 2f
                drawCircle(
                    color = Color.Transparent,
                    radius = radius,
                    center = center,
                    blendMode = BlendMode.Clear
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.82f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            CoachHelpHighlightShape.RoundRect -> {
                val cornerRadius = 18.dp.toPx()
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = topLeft,
                    size = Size(highlightWidth, highlightHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius,
                        cornerRadius
                    ),
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.82f),
                    topLeft = topLeft,
                    size = Size(highlightWidth, highlightHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius,
                        cornerRadius
                    ),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
