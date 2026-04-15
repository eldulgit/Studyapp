package com.example.studyapp.ui.settings.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.model.UserProfile
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.ProfileImageRepository
import com.example.studyapp.data.repository.UserRepository
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val profileImageRepository = ProfileImageRepository()

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var userName by mutableStateOf("")
        private set

    var profileImageUrl by mutableStateOf("")
        private set

    private fun getUidOrNull(): String? {
        return authRepository.getCurrentUid()
    }

    fun onUserNameChanged(newValue: String) {
        userName = newValue
    }

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val profile = userRepository.getUserProfile(uid)
                userProfile = profile
                userName = profile?.name ?: ""
                profileImageUrl = profile?.profileImageUrl ?: ""
                Log.d("UserVM", "loadUserProfile success / uid=$uid / imageUrl=$profileImageUrl")
            } catch (e: Exception) {
                Log.e("UserVM", "loadUserProfile 실패", e)
            }
        }
    }

    fun saveUserName() {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val trimmed = userName.trim()
                if (trimmed.isEmpty()) return@launch

                userRepository.updateUserName(uid, trimmed)
                loadUserProfile()
                Log.d("UserVM", "saveUserName success / uid=$uid")
            } catch (e: Exception) {
                Log.e("UserVM", "saveUserName 실패", e)
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                Log.d("UserVM", "업로드 시작 / uid=$uid / uri=$imageUri")

                val downloadUrl = profileImageRepository.uploadProfileImage(uid, imageUri)
                Log.d("UserVM", "Storage 업로드 성공 / downloadUrl=$downloadUrl")

                userRepository.updateProfileImageUrl(uid, downloadUrl)
                Log.d("UserVM", "Firestore URL 저장 성공")

                loadUserProfile()
            } catch (e: Exception) {
                if (e is StorageException) {
                    Log.e(
                        "UserVM",
                        "uploadProfileImage 실패 / code=${e.errorCode} / message=${e.message}",
                        e
                    )
                } else {
                    Log.e("UserVM", "uploadProfileImage 실패 / ${e.message}", e)
                }
            }
        }
    }
}