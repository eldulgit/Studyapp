package com.example.studyapp.ui.stats

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class CalendarStatsViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val studySessionRepository = StudySessionRepository()

    val dailyStudySeconds = mutableStateMapOf<String, Int>()

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    fun loadDailyStudySeconds(
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val result = studySessionRepository.getDailyStudySeconds(
                    userId = uid,
                    startDate = startDate,
                    endDate = endDate
                )

                dailyStudySeconds.clear()
                dailyStudySeconds.putAll(result)
            } catch (e: Exception) {
                android.util.Log.e("CalendarStats", "일별 집계 불러오기 실패", e)
            }
        }
    }
}