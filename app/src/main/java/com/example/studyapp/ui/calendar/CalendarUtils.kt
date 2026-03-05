package com.example.studyapp.ui.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getColorForStudyMinutes(
    minutes: Int,
    goalMinutes: Int = 240
): Color {
    val percent = minutes.toFloat() / goalMinutes * 100

    return when {
        percent >= 80 -> MaterialTheme.colorScheme.primary
        percent >= 40 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        percent > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
}
