package com.example.studyapp.ui.stats

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudySessionRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun addRecord(
        userId: String,
        subjectName: String,
        startTimeMillis: Long,
        endTimeMillis: Long,
        studiedSeconds: Int,
        sessionDate: String
    ) {
        val docRef = db.collection("users")
            .document(userId)
            .collection("study_sessions")
            .document()

        val recordData = hashMapOf(
            "id" to docRef.id,
            "subjectName" to subjectName,
            "startTimeMillis" to startTimeMillis,
            "endTimeMillis" to endTimeMillis,
            "studiedSeconds" to studiedSeconds,
            "sessionDate" to sessionDate
        )

        docRef.set(recordData).await()


    }

    suspend fun getAllRecords(userId: String): List<StudySessionRecord> {
        val result = db.collection("users")
            .document(userId)
            .collection("study_sessions")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val subjectName = doc.getString("subjectName") ?: return@mapNotNull null
            val startTimeMillis = doc.getLong("startTimeMillis") ?: return@mapNotNull null
            val endTimeMillis = doc.getLong("endTimeMillis") ?: return@mapNotNull null
            val studiedSeconds = doc.getLong("studiedSeconds")?.toInt() ?: return@mapNotNull null
            val sessionDate = doc.getString("sessionDate") ?: return@mapNotNull null

            StudySessionRecord(
                id = doc.id,
                subjectName = subjectName,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                studiedSeconds = studiedSeconds,
                sessionDate = sessionDate
            )
        }
    }

    suspend fun clearAll(userId: String) {
        val result = db.collection("users")
            .document(userId)
            .collection("study_sessions")
            .get()
            .await()

        for (doc in result.documents) {
            doc.reference.delete().await()
        }
    }

    suspend fun getRecordsByDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): List<StudySessionRecord> {
        val result = db.collection("users")
            .document(userId)
            .collection("study_sessions")
            .whereGreaterThanOrEqualTo("sessionDate", startDate)
            .whereLessThanOrEqualTo("sessionDate", endDate)
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val subjectName = doc.getString("subjectName") ?: return@mapNotNull null
            val startTimeMillis = doc.getLong("startTimeMillis") ?: return@mapNotNull null
            val endTimeMillis = doc.getLong("endTimeMillis") ?: return@mapNotNull null
            val studiedSeconds = doc.getLong("studiedSeconds")?.toInt() ?: return@mapNotNull null
            val sessionDate = doc.getString("sessionDate") ?: return@mapNotNull null

            StudySessionRecord(
                id = doc.id,
                subjectName = subjectName,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                studiedSeconds = studiedSeconds,
                sessionDate = sessionDate
            )
        }
    }

    suspend fun getDailyStudySeconds(
        userId: String,
        startDate: String,
        endDate: String
    ): Map<String, Int> {
        val records = getRecordsByDateRange(userId, startDate, endDate)

        return records
            .groupBy { it.sessionDate }
            .mapValues { entry ->
                entry.value.sumOf { it.studiedSeconds }
            }
    }
}