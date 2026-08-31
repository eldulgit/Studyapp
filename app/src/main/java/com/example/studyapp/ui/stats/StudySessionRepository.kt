package com.example.studyapp.ui.stats

import com.example.studyapp.util.AppTimeZone
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale
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

        return result.documents.mapNotNull { doc -> doc.toStudySessionRecord() }
    }

    suspend fun getTimerOverrideRecords(userId: String): List<StudySessionRecord> {
        val dateDocuments = db.collection("users")
            .document(userId)
            .collection("generated_schedules")
            .get()
            .await()

        val records = mutableListOf<StudySessionRecord>()

        dateDocuments.documents.forEach { dateDocument ->
            val sessionDate = dateDocument.getString("date")
                ?: dateDocument.id

            val overrideDocuments = dateDocument.reference
                .collection("timer_overrides")
                .get()
                .await()

            overrideDocuments.documents.forEach { overrideDocument ->
                val subjectName = overrideDocument.getString("subjectName")
                    ?.takeIf { it.isNotBlank() }
                    ?: return@forEach
                val allocatedSeconds = overrideDocument.getLong("allocatedSeconds")
                    ?.toInt()
                    ?: return@forEach
                val remainingSeconds = overrideDocument.getLong("remainingSeconds")
                    ?.toInt()
                    ?: allocatedSeconds
                val studiedSeconds = (allocatedSeconds - remainingSeconds)
                    .coerceAtLeast(0)

                if (studiedSeconds <= 0) return@forEach

                records.add(
                    StudySessionRecord(
                        id = "${dateDocument.id}_${overrideDocument.id}",
                        subjectName = subjectName,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                        studiedSeconds = studiedSeconds,
                        sessionDate = sessionDate
                    )
                )
            }
        }

        return records
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

        return result.documents.mapNotNull { doc -> doc.toStudySessionRecord() }
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

private fun DocumentSnapshot.toStudySessionRecord(): StudySessionRecord? {
    val subjectName = getString("subjectName")
        ?: getString("subject")
        ?: getString("title")
        ?: "공부"
    val startTimeMillis = getMillis("startTimeMillis", "startMillis", "startedAt")
        ?: 0L
    val endTimeMillis = getMillis("endTimeMillis", "endMillis", "endedAt")
        ?: 0L
    val studiedSeconds = getSecondsFromDb()
        ?: ((endTimeMillis - startTimeMillis) / 1000L)
            .toInt()
            .takeIf { it > 0 }
        ?: return null
    val sessionDate = getString("sessionDate")
        ?: getString("date")
        ?: startTimeMillis
            .takeIf { it > 0L }
            ?.toAppDateString()
        ?: return null

    return StudySessionRecord(
        id = id,
        subjectName = subjectName,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        studiedSeconds = studiedSeconds.coerceAtLeast(0),
        sessionDate = sessionDate
    )
}

private fun DocumentSnapshot.getSecondsFromDb(): Int? {
    return getLong("studiedSeconds")?.toInt()
        ?: getLong("studySeconds")?.toInt()
        ?: getLong("durationSeconds")?.toInt()
        ?: getLong("totalSeconds")?.toInt()
        ?: getLong("studiedMinutes")?.toInt()?.times(60)
        ?: getLong("studyMinutes")?.toInt()?.times(60)
        ?: getLong("durationMinutes")?.toInt()?.times(60)
        ?: getDouble("studiedHours")?.let { (it * 3600).toInt() }
}

private fun DocumentSnapshot.getMillis(vararg fieldNames: String): Long? {
    fieldNames.forEach { fieldName ->
        val longValue = getLong(fieldName)
        if (longValue != null) return longValue

        val timestampValue = getTimestamp(fieldName)
        if (timestampValue != null) return timestampValue.toDate().time
    }

    return null
}

private fun Long.toAppDateString(): String {
    val calendar = Calendar.getInstance(AppTimeZone.timeZone).apply {
        timeInMillis = this@toAppDateString
    }

    return String.format(
        Locale.getDefault(),
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
