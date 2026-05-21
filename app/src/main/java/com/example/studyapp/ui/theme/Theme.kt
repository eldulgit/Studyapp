package com.example.studyapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = StudyBlue,
    onPrimary = StudyText,

    secondary = StudyBlueDark,
    onSecondary = StudyText,

    tertiary = StudyYellow,
    onTertiary = StudyText,

    background = StudyBackground,
    onBackground = StudyText,

    surface = StudySurface,
    onSurface = StudyText,

    surfaceVariant = StudySurfaceVariant,
    onSurfaceVariant = StudyMutedText,
    outline = StudyOutline,
    outlineVariant = StudyOutline.copy(alpha = 0.6f)
)

private val DarkColorScheme = darkColorScheme(
    primary = StudyBlueDarkTheme,
    onPrimary = StudyText,

    secondary = StudyBlueDarkTheme,
    onSecondary = StudyText,

    tertiary = StudyYellowDarkTheme,
    onTertiary = StudyText,

    background = StudyDarkBackground,
    onBackground = StudyDarkText,

    surface = StudyDarkSurface,
    onSurface = StudyDarkText,

    surfaceVariant = StudyDarkSurfaceVariant,
    onSurfaceVariant = StudyDarkMutedText,
    outline = StudyDarkOutline,
    outlineVariant = StudyDarkOutline.copy(alpha = 0.72f)
)

@Composable
fun StudyappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
