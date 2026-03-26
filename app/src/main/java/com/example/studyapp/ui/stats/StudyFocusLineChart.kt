package com.example.studyapp.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun StudyFocusLineChart(
    points: List<HourlyFocusPoint>,
    modifier: Modifier = Modifier
) {
    val safePoints = if (points.isEmpty()) {
        listOf(
            HourlyFocusPoint(17, 0),
            HourlyFocusPoint(18, 0),
            HourlyFocusPoint(19, 0),
            HourlyFocusPoint(20, 0),
            HourlyFocusPoint(21, 0),
            HourlyFocusPoint(22, 0),
            HourlyFocusPoint(23, 0),
            HourlyFocusPoint(24, 0)
        )
    } else {
        points
    }

    val maxY = max(safePoints.maxOfOrNull { it.studiedMinutes } ?: 0, 10)

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "시간대별 집중도",
            modifier = Modifier.padding(bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val leftPadding = 20.dp.toPx()
            val rightPadding = 20.dp.toPx()
            val topPadding = 20.dp.toPx()
            val bottomPadding = 28.dp.toPx()

            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding

            if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            val stepX = if (safePoints.size > 1) {
                chartWidth / (safePoints.size - 1)
            } else {
                chartWidth
            }

            val guideCount = 4
            repeat(guideCount + 1) { index ->
                val y = topPadding + (chartHeight / guideCount) * index
                drawLine(
                    color = outlineColor.copy(alpha = 0.25f),
                    start = Offset(leftPadding, y),
                    end = Offset(size.width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val offsets = safePoints.mapIndexed { index, point ->
                val ratio = point.studiedMinutes.toFloat() / maxY.toFloat()
                val x = leftPadding + stepX * index
                val y = topPadding + chartHeight - (chartHeight * ratio)
                Offset(x, y)
            }

            if (offsets.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                offsets.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            safePoints.forEach { point ->
                Text(
                    text = "${point.hour}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}