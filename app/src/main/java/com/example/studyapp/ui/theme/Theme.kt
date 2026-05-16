package com.example.studyapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
    onSurface = StudyText
)

private val DarkColorScheme = darkColorScheme(
    primary = StudyBlueDarkTheme,
    onPrimary = StudyText,

    secondary = StudyBlueDark,
    onSecondary = StudyText,

    tertiary = StudyYellowDarkTheme,
    onTertiary = StudyText,

    background = StudyDarkBackground,
    onBackground = StudyDarkText,

    surface = StudyDarkSurface,
    onSurface = StudyDarkText
)

@Composable
fun StudyappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
