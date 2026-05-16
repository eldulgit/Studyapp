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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StudyFocusLineChart(
    points: List<HourlyFocusPoint>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 150.dp
) {
    val safePoints = if (points.isEmpty()) {
        listOf(
            HourlyFocusPoint(hour = 7, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 8, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 9, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 10, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 11, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 12, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 13, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 14, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 15, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 16, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 17, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 18, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 19, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 20, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 21, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 22, studiedMinutes = 0, focusScore = 0),
            HourlyFocusPoint(hour = 23, studiedMinutes = 0, focusScore = 0)
        )
    } else {
        points
    }

    val maxY = 100f

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "시간대별 집중도",
            modifier = Modifier.padding(bottom = 6.dp),
            style = MaterialTheme.typography.titleSmall
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
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
                val ratio = point.focusScore.toFloat() / maxY
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
            safePoints.forEachIndexed { index, point ->
                val showLabel = index == 0 ||
                        index == safePoints.lastIndex ||
                        point.hour % 2 == 0

                Text(
                    text = if (showLabel) "${point.hour}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
