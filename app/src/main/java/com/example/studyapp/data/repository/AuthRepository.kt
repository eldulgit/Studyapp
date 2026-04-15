package com.example.studyapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signInAnonymouslyIfNeeded(): String {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser.uid
        }

        return suspendCancellableCoroutine { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        cont.resume(uid)
                    } else {
                        cont.resumeWithException(IllegalStateException("익명 로그인 성공 후 uid가 없습니다."))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}