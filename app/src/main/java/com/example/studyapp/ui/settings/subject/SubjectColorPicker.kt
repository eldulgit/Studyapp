package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.theme.isAppInDarkTheme
import com.example.studyapp.ui.theme.subjectColorForTheme

@Composable
fun SubjectColorPicker(
    colors: List<Color>,
    selectedColorArgb: Int,
    disabledColorArgbList: List<Int> = emptyList(),
    onColorSelected: (Color) -> Unit
) {
    val isDarkTheme = isAppInDarkTheme()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { color ->
            val colorArgb = color.toArgb()
            val displayColor = subjectColorForTheme(color, isDarkTheme)
            val isSelected = selectedColorArgb == colorArgb
            val isDisabled = colorArgb in disabledColorArgbList

            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isDisabled) {
                                if (isDarkTheme) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color.LightGray
                                }
                            } else {
                                displayColor
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = if (isSelected) 2.dp else if (isDisabled) 1.dp else 0.dp,
                            color = when {
                                isSelected -> Color.Gray
                                isDisabled -> Color.Gray
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = !isDisabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onColorSelected(color)
                        }
                ) {
                    if (isDisabled) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawLine(
                                color = Color.Gray,
                                start = androidx.compose.ui.geometry.Offset(
                                    x = size.width * 0.25f,
                                    y = size.height * 0.75f
                                ),
                                end = androidx.compose.ui.geometry.Offset(
                                    x = size.width * 0.75f,
                                    y = size.height * 0.25f
                                ),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
