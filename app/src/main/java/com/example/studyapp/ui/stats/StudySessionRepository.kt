package com.example.studyapp.ui.stats

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StudySessionRepository {

    private val _records = MutableStateFlow<List<StudySessionRecord>>(emptyList())
    val records: StateFlow<List<StudySessionRecord>> = _records.asStateFlow()

    fun addRecord(record: StudySessionRecord) {
        _records.value = _records.value + record
    }

    fun clearAll() {
        _records.value = emptyList()
    }
}