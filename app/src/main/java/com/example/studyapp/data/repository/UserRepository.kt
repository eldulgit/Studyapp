package com.example.studyapp.data.repository

import com.example.studyapp.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserProfile(uid: String): UserProfile? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) return null

        return snapshot.toObject(UserProfile::class.java)
    }

    suspend fun updateUserName(uid: String, newName: String) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "name" to newName,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun updateProfileImageUrl(uid: String, imageUrl: String) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "profileImageUrl" to imageUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun ensureUserDocument(uid: String, isGuest: Boolean) {
        val userRef = db.collection("users").document(uid)

        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val data = hashMapOf(
                "uid" to uid,
                "name" to "",
                "profileImageUrl" to "",
                "isGuest" to isGuest,

                "wakeTime" to "",
                "sleepTime" to "",
                "exercise" to false,
                "lifestyleCompleted" to false,

                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            userRef.set(data).await()
        } else {
            userRef.set(
                mapOf(
                    "uid" to uid,
                    "isGuest" to isGuest,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    suspend fun isLifestyleCompleted(uid: String): Boolean {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.getBoolean("lifestyleCompleted") == true
    }

    suspend fun saveLifestyle(
        uid: String,
        wakeTime: String,
        sleepTime: String,
        exercise: Boolean
    ) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "wakeTime" to wakeTime,
                    "sleepTime" to sleepTime,
                    "exercise" to exercise,
                    "lifestyleCompleted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }
}