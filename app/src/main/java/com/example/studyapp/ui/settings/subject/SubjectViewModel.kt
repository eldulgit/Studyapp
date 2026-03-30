package com.example.studyapp.ui.settings.subject

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.studyapp.data.repository.SubjectRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.studyapp.data.repository.AuthRepository

class SubjectViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private fun getUidOrNull(): String? {
        return authRepository.getCurrentUid()
    }
    private val repository = SubjectRepository()
    val subjects = mutableStateListOf<SubjectItem>()

    fun removeSubjectFromFirestore(id: String) {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch
                repository.deleteSubject(uid, id)
                loadSubjectsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("SubjectFirestore", "삭제 실패", e)
            }
        }
    }

    fun loadSubjectsFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch
                val result = repository.getSubjects(uid)

                subjects.clear()

                subjects.addAll(
                    result.map {
                        SubjectItem(
                            id = it.id,
                            name = it.name,
                            priority = it.priority,
                            colorArgb = it.colorArgb
                        )
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addSubject(name: String, priority: Int, colorArgb: Int): Boolean {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) return false

        val isDuplicateName = subjects.any { it.name == trimmedName }
        if (isDuplicateName) return false

        val isDuplicateColor = subjects.any { it.colorArgb == colorArgb }
        if (isDuplicateColor) return false

        subjects.add(
            SubjectItem(
                id="",
                name = trimmedName,
                priority = priority,
                colorArgb = colorArgb
            )
        )
        return true
    }

    fun removeSubject(id: String) {
        subjects.removeAll { it.id == id }
    }

    fun addSubjectToFirestore(
        name: String,
        priority: Int,
        colorArgb: Int
    ) {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            //android.util.Log.e("SubjectFirestore", "저장 중단: 과목명이 비어 있음")
            return
        }

        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch

                repository.addSubject(
                    userId = uid,
                    name = trimmedName,
                    priority = priority,
                    colorArgb = colorArgb
                )
                android.util.Log.e("SubjectFirestore", "저장 성공: $trimmedName")
                loadSubjectsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.e("SubjectFirestore", "저장 실패", e)
            }
        }
    }

    fun updateSubjectInFirestore(
        id: String,
        newName: String,
        newPriority: Int,
        newColorArgb: Int
    ) {
        viewModelScope.launch {
            try {
                if (id.isBlank()) {
                    android.util.Log.e("SubjectFirestore", "수정 중단: id가 비어 있음")
                    return@launch
                }

                val uid = getUidOrNull() ?: return@launch

                repository.updateSubject(
                    userId = uid,
                    id = id,
                    newName = newName,
                    newPriority = newPriority,
                    newColorArgb = newColorArgb
                )

                android.util.Log.e("SubjectFirestore", "ViewModel 수정 성공")
                loadSubjectsFromFirestore()

            } catch (e: Exception) {
                android.util.Log.e("SubjectFirestore", "ViewModel 수정 실패", e)
            }
        }
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

        val oldItem = subjects[index]

        subjects[index] = SubjectItem(
            id = oldItem.id,   // 🔥 기존 id 유지
            name = trimmedName,
            priority = newPriority,
            colorArgb = newColorArgb
        )
        return true
    }
}