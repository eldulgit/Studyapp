package com.example.studyapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
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

    fun isCurrentUserAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous == true
    }

    fun isCurrentUserGoogleLinked(): Boolean {
        val currentUser = auth.currentUser ?: return false

        return !currentUser.isAnonymous &&
                currentUser.providerData.any { provider ->
                    provider.providerId == GoogleAuthProvider.PROVIDER_ID
                }
    }

    suspend fun signInWithGoogle(activity: Activity): String {
        val credentialManager = CredentialManager.create(activity)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = "646379260727-3ltc0ke5s97s45272chr9cdmhb62uqdp.apps.googleusercontent.com"
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = try {
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        } catch (e: NoCredentialException) {
            throw IllegalStateException(
                "기기에서 사용할 수 있는 Google 계정을 찾지 못했습니다. 에뮬레이터/기기에 Google 계정이 로그인되어 있는지 확인해주세요.",
                e
            )
        }

        val googleIdTokenCredential = try {
            GoogleIdTokenCredential.createFrom(result.credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google ID 토큰 파싱 실패", e)
        }

        val firebaseCredential =
            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

        val currentUser = auth.currentUser

        val authResult = if (currentUser != null && currentUser.isAnonymous) {
            try {
                currentUser.linkWithCredential(firebaseCredential).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                auth.signInWithCredential(firebaseCredential).await()
            }
        } else {
            auth.signInWithCredential(firebaseCredential).await()
        }

        return authResult.user?.uid
            ?: throw IllegalStateException("Google 로그인 성공 후 uid가 없습니다.")
    }

    suspend fun getStatsOwnerId(): String {
        val currentUser = auth.currentUser

        return if (currentUser != null) {
            currentUser.uid
        } else {
            signInAnonymouslyIfNeeded()
        }
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

    suspend fun getStudyOwnerId(): String {
        val currentUser = auth.currentUser

        // Google 로그인 사용자
        if (currentUser != null && !currentUser.isAnonymous) {
            val googleProviderUid = currentUser.providerData
                .firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                ?.uid

            if (!googleProviderUid.isNullOrBlank()) {
                return "google_$googleProviderUid"
            }

            // 혹시 Google provider 정보를 못 가져오는 경우 대비
            return "login_${currentUser.uid}"
        }

        // 비회원 사용자
        val guestUid = signInAnonymouslyIfNeeded()
        return "guest_$guestUid"
    }

    fun signOut() {
        auth.signOut()
    }
}
