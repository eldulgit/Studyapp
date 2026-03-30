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
            .update(
                mapOf(
                    "name" to newName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
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
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            userRef.set(data, SetOptions.merge()).await()
        }
    }
}