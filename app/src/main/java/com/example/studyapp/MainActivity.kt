package com.example.studyapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.ui.navigation.MainScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val uid = authRepository.signInAnonymouslyIfNeeded()
                userRepository.ensureUserDocument(uid = uid, isGuest = true)
                Log.d("AuthTest", "현재 uid = $uid")
            } catch (e: Exception) {
                Log.e("AuthTest", "초기 사용자 설정 실패", e)
            }
        }

        setContent {
            MainScreen()
        }
    }
}