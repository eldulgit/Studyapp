package com.example.studyapp.ui.settings.account

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.studyapp.data.repository.UserRepository

@Composable
fun AccountSettingScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    var loginMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "계정",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = {
                    val safeActivity = activity ?: return@Button

                    scope.launch {
                        try {
                            loginMessage = null

                            val uid = authRepository.signInWithGoogle(safeActivity)

                            userRepository.ensureUserDocument(
                                uid = uid,
                                isGuest = false
                            )

                            android.util.Log.d("GoogleLogin", "Google 로그인 성공 / uid=$uid")
                            loginMessage = "Google 로그인 성공"
                        } catch (e: Exception) {
                            loginMessage = e.message ?: "Google 로그인 실패"
                            android.util.Log.e("GoogleLogin", "Google 로그인 실패", e)
                        }
                    }
                }
            ) {
                Text("Google로 로그인")
            }

            loginMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    authRepository.signOut()

                    Toast.makeText(
                        context,
                        "로그아웃되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("로그아웃")
            }
        }
    }
}