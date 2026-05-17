package com.example.studyapp.data.repository

import com.example.studyapp.data.model.GeneratedScheduleItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

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

    private fun timerOverridesCollection(
        userId: String,
        date: String
    ) = scheduleDateDocument(userId, date)
        .collection("timer_overrides")

    suspend fun saveTimerTimeOverride(
        userId: String,
        date: String,
        timerId: Long,
        subjectName: String,
        allocatedSeconds: Int,
        remainingSeconds: Int
    ) {
        val dateDocument = scheduleDateDocument(userId, date)

        dateDocument.set(
            mapOf(
                "date" to date,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        timerOverridesCollection(userId, date)
            .document(timerId.toString())
            .set(
                hashMapOf<String, Any?>(
                    "timerId" to timerId,
                    "subjectName" to subjectName,
                    "allocatedSeconds" to allocatedSeconds,
                    "remainingSeconds" to remainingSeconds,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun deleteTimerOverridesBySubjectName(
        userId: String,
        subjectName: String
    ) {
        val trimmedName = subjectName.trim()

        if (trimmedName.isBlank()) return

        val dateDocuments = db.collection("users")
            .document(userId)
            .collection("generated_schedules")
            .get()
            .await()

        val batch = db.batch()
        var deleteCount = 0

        dateDocuments.documents.forEach { dateDocument ->
            val overrideDocuments = dateDocument.reference
                .collection("timer_overrides")
                .whereEqualTo("subjectName", trimmedName)
                .get()
                .await()

            overrideDocuments.documents.forEach { overrideDocument ->
                batch.delete(overrideDocument.reference)
                deleteCount++
            }
        }

        if (deleteCount > 0) {
            batch.commit().await()
        }
    }

    suspend fun getTimerTimeOverrides(
        userId: String,
        date: String
    ): Map<Long, TimerTimeOverride> {
        val result = timerOverridesCollection(userId, date)
            .get()
            .await()

        return result.documents.mapNotNull { document ->
            val timerId = document.getLong("timerId")
                ?: document.id.toLongOrNull()
                ?: return@mapNotNull null

            val allocatedSeconds = document.getLong("allocatedSeconds")?.toInt()
                ?: return@mapNotNull null

            val remainingSeconds = document.getLong("remainingSeconds")?.toInt()
                ?: allocatedSeconds

            timerId to TimerTimeOverride(
                timerId = timerId,
                subjectName = document.getString("subjectName").orEmpty(),
                allocatedSeconds = allocatedSeconds,
                remainingSeconds = remainingSeconds
            )
        }.toMap()
    }
}

data class TimerTimeOverride(
    val timerId: Long,
    val subjectName: String,
    val allocatedSeconds: Int,
    val remainingSeconds: Int
)