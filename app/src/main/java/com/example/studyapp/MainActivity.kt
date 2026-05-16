package com.example.studyapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studyapp.ui.StudyApp
import com.example.studyapp.ui.settings.SettingsViewModel
import com.example.studyapp.ui.theme.StudyappTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val systemDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = when (settingsViewModel.selectedTheme) {
                "light" -> false
                "dark" -> true
                else -> systemDarkTheme
            }

            StudyappTheme(
                darkTheme = useDarkTheme,
                dynamicColor = false
            ) {
                StudyApp()
            }
        }
    }
}
