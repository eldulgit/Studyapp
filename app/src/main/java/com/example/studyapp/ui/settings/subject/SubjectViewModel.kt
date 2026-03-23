package com.example.studyapp.ui.settings.subject

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class SubjectViewModel(application: Application) : AndroidViewModel(application) {

    var subjects by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    fun addSubject(name: String, priority: Int) {
        if (subjects.none { it.first == name }) {
            subjects = subjects + (name to priority)
        }
    }

    fun removeSubject(name: String) {
        subjects = subjects.filterNot { it.first == name }
    }

    fun updateSubject(oldName: String, newName: String, newPriority: Int) {
        if (oldName != newName && subjects.any { it.first == newName }) {
            return
        }

        subjects = subjects.map { subject ->
            if (subject.first == oldName) {
                newName to newPriority
            } else {
                subject
            }
        }
    }

}