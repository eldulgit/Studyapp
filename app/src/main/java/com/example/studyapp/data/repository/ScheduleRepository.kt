package com.example.studyapp.data.repository

import com.example.studyapp.ui.settings.schedule.ScheduleItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScheduleRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addSchedule(
        userId: String,
        subjectId: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        type: String
    ) {
        val docRef = db.collection("users")
            .document(userId)
            .collection("schedules")
            .document()

        val scheduleData = hashMapOf(
            "id" to docRef.id,
            "subjectId" to subjectId,
            "dayOfWeek" to dayOfWeek,
            "startTime" to startTime,
            "endTime" to endTime,
            "type" to type
        )

        docRef.set(scheduleData).await()
    }

    suspend fun getSchedules(userId: String): List<ScheduleItem> {
        val result = db.collection("users")
            .document(userId)
            .collection("schedules")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val subjectId = doc.getString("subjectId") ?: return@mapNotNull null
            val dayOfWeek = doc.getString("dayOfWeek") ?: return@mapNotNull null
            val startTime = doc.getString("startTime") ?: return@mapNotNull null
            val endTime = doc.getString("endTime") ?: return@mapNotNull null
            val type = doc.getString("type") ?: return@mapNotNull null

            ScheduleItem(
                id = doc.id,
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                type = type
            )
        }
    }

    suspend fun deleteSchedule(userId: String, id: String) {
        if (id.isBlank()) return

        db.collection("users")
            .document(userId)
            .collection("schedules")
            .document(id)
            .delete()
            .await()
    }

    suspend fun updateSchedule(
        userId: String,
        id: String,
        subjectId: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        type: String
    ) {
        if (id.isBlank()) return

        db.collection("users")
            .document(userId)
            .collection("schedules")
            .document(id)
            .update(
                mapOf(
                    "subjectId" to subjectId,
                    "dayOfWeek" to dayOfWeek,
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "type" to type
                )
            )
            .await()
    }
}