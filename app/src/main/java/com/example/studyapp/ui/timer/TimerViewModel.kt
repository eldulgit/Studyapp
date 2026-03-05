package com.example.studyapp.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    var studiedMinutes by mutableStateOf(0)
        private set

    fun addMinutes(minutes: Int) {
        studiedMinutes += minutes
    }

    fun reset() {
        studiedMinutes = 0
    }
}
