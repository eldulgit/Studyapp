package com.example.studyapp.ui.settings.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.model.UserProfile
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var userName by mutableStateOf("")
        private set

    private fun getUidOrNull(): String? {
        return authRepository.getCurrentUid()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch
                val profile = userRepository.getUserProfile(uid)
                userProfile = profile
                userName = profile?.name ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onUserNameChanged(newValue: String) {
        userName = newValue
    }

    fun saveUserName() {
        viewModelScope.launch {
            try {
                val uid = getUidOrNull() ?: return@launch
                val trimmed = userName.trim()
                if (trimmed.isEmpty()) return@launch

                userRepository.updateUserName(uid, trimmed)
                loadUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}