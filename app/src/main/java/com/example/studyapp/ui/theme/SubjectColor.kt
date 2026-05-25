package com.example.studyapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

fun subjectColorForTheme(
    color: Color,
    darkTheme: Boolean
): Color {
    if (!darkTheme) return color

    return lerp(
        start = color,
        stop = StudyDarkSurfaceVariant,
        fraction = 0.22f
    ).copy(alpha = color.alpha)
}
