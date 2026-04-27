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
import com.example.studyapp.ui.StudyApp
import com.example.studyapp.ui.navigation.MainScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val uid = try {
                val result = authRepository.signInAnonymouslyIfNeeded()
                Log.d("AuthTest", "익명 로그인 성공 / uid = $result")
                result
            } catch (e: Exception) {
                Log.e("AuthTest", "익명 로그인 실패", e)
                return@launch
            }

            try {
                userRepository.ensureUserDocument(uid = uid, isGuest = true)
                Log.d("AuthTest", "users 문서 생성 성공 / uid = $uid")
            } catch (e: Exception) {
                Log.e("AuthTest", "users 문서 생성 실패 / uid = $uid", e)
            }
        }

        setContent {
            StudyApp()
        }
    }
}