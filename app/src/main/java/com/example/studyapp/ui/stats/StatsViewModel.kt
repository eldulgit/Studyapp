package com.example.studyapp.ui.stats

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val studySessionRepository = StudySessionRepository()

    val records = mutableStateListOf<StudySessionRecord>()

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    fun loadRecordsFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val result = studySessionRepository.getAllRecords(uid)
                records.clear()
                records.addAll(result)
            } catch (e: Exception) {
                android.util.Log.e("StatsFirestore", "기록 불러오기 실패", e)
            }
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                studySessionRepository.clearAll(uid)
                records.clear()
            } catch (e: Exception) {
                android.util.Log.e("StatsFirestore", "기록 삭제 실패", e)
            }
        }
    }
}