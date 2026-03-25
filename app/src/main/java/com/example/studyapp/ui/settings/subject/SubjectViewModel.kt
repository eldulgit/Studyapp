package com.example.studyapp.ui.settings.subject

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class SubjectViewModel : ViewModel() {

    val subjects = mutableStateListOf<SubjectItem>()

    fun addSubject(name: String, priority: Int, colorArgb: Int): Boolean {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) return false

        val isDuplicateName = subjects.any { it.name == trimmedName }
        if (isDuplicateName) return false

        val isDuplicateColor = subjects.any { it.colorArgb == colorArgb }
        if (isDuplicateColor) return false

        subjects.add(
            SubjectItem(
                name = trimmedName,
                priority = priority,
                colorArgb = colorArgb
            )
        )
        return true
    }

    fun removeSubject(name: String) {
        subjects.removeAll { it.name == name }
    }

    fun updateSubject(
        oldName: String,
        newName: String,
        newPriority: Int,
        newColorArgb: Int
    ): Boolean {
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) return false

        val isDuplicateName = subjects.any {
            it.name == trimmedName && it.name != oldName
        }
        if (isDuplicateName) return false

        val isDuplicateColor = subjects.any {
            it.colorArgb == newColorArgb && it.name != oldName
        }
        if (isDuplicateColor) return false

        val index = subjects.indexOfFirst { it.name == oldName }
        if (index == -1) return false

        subjects[index] = SubjectItem(
            name = trimmedName,
            priority = newPriority,
            colorArgb = newColorArgb
        )
        return true
    }
}