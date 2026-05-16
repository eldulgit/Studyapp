package com.example.studyapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.studyapp.ui.StudyApp
import com.example.studyapp.ui.theme.StudyappTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StudyappTheme(dynamicColor = false) {
                StudyApp()
            }
        }
    }
}