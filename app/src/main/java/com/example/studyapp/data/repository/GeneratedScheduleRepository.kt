package com.example.studyapp.data.repository

import com.example.studyapp.data.model.GeneratedScheduleItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GeneratedScheduleRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun scheduleDateDocument(
        userId: String,
        date: String
    ) = db.collection("users")
        .document(userId)
        .collection("generated_schedules")
        .document(date)

    private fun scheduleItemsCollection(
        userId: String,
        date: String
    ) = scheduleDateDocument(userId, date)
        .collection("items")

    suspend fun replaceSchedulesForDate(
        userId: String,
        date: String,
        schedules: List<GeneratedScheduleItem>
    ) {
        val dateDocument = scheduleDateDocument(userId, date)
        val itemsCollection = scheduleItemsCollection(userId, date)

        val oldSchedules = itemsCollection
            .get()
            .await()

        val batch = db.batch()

        batch.set(
            dateDocument,
            hashMapOf(
                "date" to date,
                "count" to schedules.size,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        oldSchedules.documents.forEach { document ->
            batch.delete(document.reference)
        }

        schedules.forEach { schedule ->
            val document = itemsCollection.document()

            val data = hashMapOf<String, Any?>(
                "id" to document.id,
                "date" to date,
                "title" to schedule.title,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "subjectId" to schedule.subjectId,
                "colorArgb" to schedule.colorArgb,
                "priority" to schedule.priority,
                "isCompleted" to schedule.isCompleted,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            batch.set(document, data)
        }

        batch.commit().await()
    }

    suspend fun getSchedulesByDate(
        userId: String,
        date: String
    ): List<GeneratedScheduleItem> {
        val result = scheduleItemsCollection(userId, date)
            .get()
            .await()

        return result.documents.mapNotNull { document ->
            val title = document.getString("title") ?: return@mapNotNull null
            val startTime = document.getString("startTime") ?: return@mapNotNull null
            val endTime = document.getString("endTime") ?: return@mapNotNull null

            GeneratedScheduleItem(
                id = document.id,
                date = document.getString("date").orEmpty(),
                title = title,
                startTime = startTime,
                endTime = endTime,
                subjectId = document.getString("subjectId"),
                colorArgb = document.getLong("colorArgb")?.toInt() ?: 0,
                priority = document.getLong("priority")?.toInt() ?: 1,
                isCompleted = document.getBoolean("isCompleted") ?: false
            )
        }.sortedBy { it.startTime }
    }
}