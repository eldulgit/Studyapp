package com.example.studyapp.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.UserRepository
import com.example.studyapp.ui.navigation.MainScreen
import com.example.studyapp.ui.onboarding.LifestyleInputScreen
import com.example.studyapp.ui.onboarding.LoginChoiceScreen
import kotlinx.coroutines.launch

private enum class StartScreen {
    Loading,
    LoginChoice,
    LifestyleInput,
    Main
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudyApp() {
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var currentScreen by remember { mutableStateOf(StartScreen.Loading) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    suspend fun moveToNextScreen(uid: String, isGuest: Boolean) {
        userRepository.ensureUserDocument(
            uid = uid,
            isGuest = isGuest
        )

        currentScreen = if (userRepository.isLifestyleCompleted(uid)) {
            StartScreen.Main
        } else {
            StartScreen.LifestyleInput
        }
    }

    fun onGoogleLoginClick() {
        if (isProcessing) return

        scope.launch {
            isProcessing = true
            errorMessage = null

            try {
                val uid = authRepository.signInWithGoogle(activity)

                userRepository.ensureUserDocument(
                    uid = uid,
                    isGuest = false
                )

                currentScreen = if (userRepository.isLifestyleCompleted(uid)) {
                    StartScreen.Main
                } else {
                    StartScreen.LifestyleInput
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Google 로그인 중 오류가 발생했습니다."
            } finally {
                isProcessing = false
            }
        }
    }

    fun onGuestClick() {
        if (isProcessing) return

        scope.launch {
            isProcessing = true
            errorMessage = null

            try {
                val uid = authRepository.signInAnonymouslyIfNeeded()

                moveToNextScreen(
                    uid = uid,
                    isGuest = true
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "비회원 로그인 중 오류가 발생했습니다."
            } finally {
                isProcessing = false
            }
        }
    }

    fun onCompleteClick(
        wakeTime: String,
        sleepTime: String,
        exercise: Boolean
    ) {
        if (isProcessing) return

        scope.launch {
            isProcessing = true
            errorMessage = null

            try {
                val uid = authRepository.getCurrentUid()
                    ?: throw IllegalStateException("로그인 정보가 없습니다. 다시 로그인해주세요.")

                userRepository.saveLifestyle(
                    uid = uid,
                    wakeTime = wakeTime,
                    sleepTime = sleepTime,
                    exercise = exercise
                )

                currentScreen = StartScreen.Main
            } catch (e: Exception) {
                errorMessage = e.message ?: "생활패턴 저장 중 오류가 발생했습니다."
            } finally {
                isProcessing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val uid = authRepository.getCurrentUid()

        if (uid == null) {
            currentScreen = StartScreen.LoginChoice
        } else {
            try {
                moveToNextScreen(
                    uid = uid,
                    isGuest = authRepository.isCurrentUserAnonymous()
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "사용자 정보를 확인하는 중 오류가 발생했습니다."
                currentScreen = StartScreen.LoginChoice
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                StartScreen.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                StartScreen.LoginChoice -> {
                    LoginChoiceScreen(
                        onGoogleLoginClick = ::onGoogleLoginClick,
                        onGuestClick = ::onGuestClick
                    )
                }

                StartScreen.LifestyleInput -> {
                    LifestyleInputScreen(
                        onCompleteClick = ::onCompleteClick
                    )
                }

                StartScreen.Main -> {
                    MainScreen(
                        onLogout = {
                            currentScreen = StartScreen.LoginChoice
                        }
                    )
                }
            }

            if (isProcessing && currentScreen != StartScreen.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage?.let {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("Activity를 찾을 수 없습니다.")
    }
}