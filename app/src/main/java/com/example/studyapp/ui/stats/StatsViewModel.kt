package com.example.studyapp.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {

    var selectedPeriod by mutableStateOf(StatsPeriod.DAILY)
        private set

    fun changePeriod(period: StatsPeriod) {
        selectedPeriod = period
    }
}
