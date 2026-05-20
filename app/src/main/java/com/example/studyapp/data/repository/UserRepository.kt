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

    suspend fun ensureUserDocument(uid: String, isGuest: Boolean) {
        val userRef = db.collection("users").document(uid)

        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val data = hashMapOf(
                "uid" to uid,
                "name" to "",
                "isGuest" to isGuest,

                "wakeTime" to "",
                "sleepTime" to "",
                "lunchStartTime" to "",
                "lunchEndTime" to "",
                "dinnerStartTime" to "",
                "dinnerEndTime" to "",
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

        if (!snapshot.exists()) return false

        val requiredFields = listOf(
            "wakeTime",
            "sleepTime",
            "lunchStartTime",
            "lunchEndTime",
            "dinnerStartTime",
            "dinnerEndTime"
        )

        val hasAllRequiredValues = requiredFields.all { fieldName ->
            !snapshot.getString(fieldName).isNullOrBlank()
        }

        return snapshot.getBoolean("lifestyleCompleted") == true && hasAllRequiredValues
    }

    suspend fun saveLifestyle(
        uid: String,
        wakeTime: String,
        sleepTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        dinnerStartTime: String,
        dinnerEndTime: String
    ) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "wakeTime" to wakeTime,
                    "sleepTime" to sleepTime,
                    "lunchStartTime" to lunchStartTime,
                    "lunchEndTime" to lunchEndTime,
                    "dinnerStartTime" to dinnerStartTime,
                    "dinnerEndTime" to dinnerEndTime,
                    "lifestyleCompleted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun saveNotificationSettings(
        uid: String,
        enabled: Boolean,
        hour: String,
        minute: String
    ) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "notificationEnabled" to enabled,
                    "notificationHour" to hour,
                    "notificationMinute" to minute,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun getNotificationSettings(uid: String): UserNotificationSettings? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) return null

        val hasNotificationSettings =
            snapshot.contains("notificationHour") ||
                    snapshot.contains("notificationMinute") ||
                    snapshot.contains("notification_hour") ||
                    snapshot.contains("notification_minute")

        if (!hasNotificationSettings) return null

        return UserNotificationSettings(
            enabled = snapshot.getBoolean("notificationEnabled")
                ?: snapshot.getBoolean("notification_enabled")
                ?: true,
            hour = snapshot.readTimePart(
                primaryField = "notificationHour",
                fallbackField = "notification_hour",
                defaultValue = "08"
            ),
            minute = snapshot.readTimePart(
                primaryField = "notificationMinute",
                fallbackField = "notification_minute",
                defaultValue = "00"
            )
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.readTimePart(
        primaryField: String,
        fallbackField: String,
        defaultValue: String
    ): String {
        val rawValue = get(primaryField) ?: get(fallbackField) ?: return defaultValue
        val digits = rawValue.toString().filter { it.isDigit() }

        if (digits.isBlank()) return defaultValue

        return digits
            .toIntOrNull()
            ?.coerceAtLeast(0)
            ?.toString()
            ?.padStart(2, '0')
            ?: defaultValue
    }
}

data class UserNotificationSettings(
    val enabled: Boolean,
    val hour: String,
    val minute: String
)
