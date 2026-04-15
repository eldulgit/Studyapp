package com.example.studyapp.data.repository

import com.example.studyapp.ui.settings.schedule.ScheduleItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScheduleRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addSchedule(
        userId: String,
        title: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        val docRef = db.collection("users")
            .document(userId)
            .collection("fixed_schedules")
            .document()

        val scheduleData = hashMapOf(
            "id" to docRef.id,
            "title" to title,
            "dayOfWeek" to dayOfWeek,
            "startTime" to startTime,
            "endTime" to endTime
        )

        docRef.set(scheduleData).await()
    }

    suspend fun getSchedules(userId: String): List<ScheduleItem> {
        val result = db.collection("users")
            .document(userId)
            .collection("fixed_schedules")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val dayOfWeek = doc.getString("dayOfWeek") ?: return@mapNotNull null
            val startTime = doc.getString("startTime") ?: return@mapNotNull null
            val endTime = doc.getString("endTime") ?: return@mapNotNull null

            ScheduleItem(
                id = doc.id,
                title = title,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime
            )
        }
    }

    suspend fun deleteSchedule(userId: String, id: String) {
        if (id.isBlank()) return

        db.collection("users")
            .document(userId)
            .collection("fixed_schedules")
            .document(id)
            .delete()
            .await()
    }

    suspend fun updateSchedule(
        userId: String,
        id: String,
        title: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        if (id.isBlank()) return

        db.collection("users")
            .document(userId)
            .collection("fixed_schedules")
            .document(id)
            .update(
                mapOf(
                    "title" to title,
                    "dayOfWeek" to dayOfWeek,
                    "startTime" to startTime,
                    "endTime" to endTime
                )
            )
            .await()
    }
}