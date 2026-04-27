package com.example.studyapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.studyapp.ui.navigation.MainScreen
import com.example.studyapp.ui.onboarding.LifestyleInputScreen
import com.example.studyapp.ui.onboarding.LoginChoiceScreen

private enum class StartScreen {
    LoginChoice,
    LifestyleInput,
    Main
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudyApp() {
    var currentScreen by remember {
        mutableStateOf(StartScreen.LoginChoice)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            StartScreen.LoginChoice -> {
                LoginChoiceScreen(
                    onGoogleLoginClick = {
                        currentScreen = StartScreen.LifestyleInput
                    },
                    onGuestClick = {
                        currentScreen = StartScreen.LifestyleInput
                    }
                )
            }

            StartScreen.LifestyleInput -> {
                LifestyleInputScreen(
                    onCompleteClick = { _, _, _ ->
                        currentScreen = StartScreen.Main
                    }
                )
            }

            StartScreen.Main -> {
                MainScreen()
            }
        }
    }
}