package com.example.studyapp.ui.settings.lifestyle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class LifeStyleViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    var wakeTime by mutableStateOf("")
        private set

    var sleepTime by mutableStateOf("")
        private set

    var lunchStartTime by mutableStateOf("")
        private set

    var lunchEndTime by mutableStateOf("")
        private set

    var dinnerStartTime by mutableStateOf("")
        private set

    var dinnerEndTime by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var saveCompleted by mutableStateOf(false)
        private set

    fun loadLifestyle() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val uid = authRepository.getCurrentUid()
                    ?: throw IllegalStateException("로그인 정보가 없습니다. 다시 로그인해주세요.")

                val profile = userRepository.getUserProfile(uid)

                wakeTime = profile?.wakeTime.orEmpty()
                sleepTime = profile?.sleepTime.orEmpty()
                lunchStartTime = profile?.lunchStartTime.orEmpty()
                lunchEndTime = profile?.lunchEndTime.orEmpty()
                dinnerStartTime = profile?.dinnerStartTime.orEmpty()
                dinnerEndTime = profile?.dinnerEndTime.orEmpty()
            } catch (e: Exception) {
                errorMessage = e.message ?: "생활패턴을 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun saveLifestyle(
        wakeTime: String,
        sleepTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        dinnerStartTime: String,
        dinnerEndTime: String
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            saveCompleted = false

            try {
                val uid = authRepository.getCurrentUid()
                    ?: throw IllegalStateException("로그인 정보가 없습니다. 다시 로그인해주세요.")

                userRepository.saveLifestyle(
                    uid = uid,
                    wakeTime = wakeTime,
                    sleepTime = sleepTime,
                    lunchStartTime = lunchStartTime,
                    lunchEndTime = lunchEndTime,
                    dinnerStartTime = dinnerStartTime,
                    dinnerEndTime = dinnerEndTime
                )

                this@LifeStyleViewModel.wakeTime = wakeTime
                this@LifeStyleViewModel.sleepTime = sleepTime
                this@LifeStyleViewModel.lunchStartTime = lunchStartTime
                this@LifeStyleViewModel.lunchEndTime = lunchEndTime
                this@LifeStyleViewModel.dinnerStartTime = dinnerStartTime
                this@LifeStyleViewModel.dinnerEndTime = dinnerEndTime

                saveCompleted = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "생활패턴 저장 중 오류가 발생했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun consumeSaveCompleted() {
        saveCompleted = false
    }
}