package com.example.studyapp.ui.timer

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TimerControlButtons(
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    Button(onClick = onToggle) {
        Text(if (isRunning) "⏸ Pause" else "▶ Start")
    }
}