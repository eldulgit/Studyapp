package com.example.studyapp.ui.settings.profile

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun onUserNameChanged(newValue: String) {
        userName = newValue
    }

    private suspend fun getOrCreateUid(): String {
        val uid = authRepository.signInAnonymouslyIfNeeded()
        userRepository.ensureUserDocument(uid, isGuest = true)
        return uid
    }

    fun loadUserProfile(context: Context) {
        viewModelScope.launch {
            try {
                val uid = getOrCreateUid()
                val profile = userRepository.getUserProfile(uid)
                userProfile = profile
                userName = profile?.name.orEmpty()
                profileImageUrl = profileImageRepository.getSavedProfileImageUri(
                    context.applicationContext
                )
                Log.d("UserVM", "loadUserProfile success / uid=$uid")
            } catch (e: Exception) {
                Log.e("UserVM", "loadUserProfile failed", e)
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
                userName = trimmed
                Log.d("UserVM", "saveUserName success / uid=$uid")
            } catch (e: Exception) {
                Log.e("UserVM", "saveUserName failed", e)
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val savedUri = withContext(Dispatchers.IO) {
                    profileImageRepository.saveProfileImage(
                        context.applicationContext,
                        imageUri
                    )
                }
                profileImageUrl = savedUri
                Log.d("UserVM", "save local profile image success / uri=$savedUri")
            } catch (e: Exception) {
                Log.e("UserVM", "save local profile image failed", e)
            }
        }
    }
}
