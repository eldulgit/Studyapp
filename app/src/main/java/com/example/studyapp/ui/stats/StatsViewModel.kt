package com.example.studyapp.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.model.UserProfile
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val studySessionRepository = StudySessionRepository()

    val records = mutableStateListOf<StudySessionRecord>()

    /**
     * 통계 데이터 조회/저장에 사용할 사용자 ID를 가져오는 함수
     *
     * - Google 로그인 상태: FirebaseAuth의 현재 사용자 uid 사용
     * - 비회원 상태: 익명 로그인 uid 사용
     */
    private suspend fun getStatsOwnerId(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        val isGuest = authRepository.isCurrentUserAnonymous()

        userRepository.ensureUserDocument(
            uid = uid,
            isGuest = isGuest
        )

        return uid
    }

    fun loadRecordsFromFirestore() {
        viewModelScope.launch {
            try {
                val uid = getStatsOwnerId()

                val result = studySessionRepository.getAllRecords(uid)

                records.clear()
                records.addAll(result)

            } catch (e: Exception) {
                android.util.Log.e("StatsFirestore", "기록 불러오기 실패", e)
            }
        }
    }

    fun loadStatsData() {
        viewModelScope.launch {
            try {
                val uid = getStatsOwnerId()

                val profile = userRepository.getUserProfile(uid)
                val result = studySessionRepository.getAllRecords(uid)

                userProfile = profile

                records.clear()
                records.addAll(result)

            } catch (e: Exception) {
                android.util.Log.e("StatsFirestore", "통계 데이터 불러오기 실패", e)
            }
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            try {
                val uid = getStatsOwnerId()

                studySessionRepository.clearAll(uid)

                records.clear()

            } catch (e: Exception) {
                android.util.Log.e("StatsFirestore", "기록 삭제 실패", e)
            }
        }
    }
}