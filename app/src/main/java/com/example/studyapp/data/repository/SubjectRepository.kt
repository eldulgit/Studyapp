package com.example.studyapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.studyapp.ui.settings.subject.SubjectItem

class SubjectRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun updateSubject(
        userId: String,
        id: String,
        newName: String,
        newPriority: Int,
        newColorArgb: Int
    ) {
        if (id.isBlank()) {
            android.util.Log.e("SubjectFirestore", "수정 중단: id가 비어 있음")
            return
        }

        android.util.Log.e("SubjectFirestore", "수정 시도 id: $id, name: $newName")

        db.collection("users")
            .document(userId)
            .collection("subjects")
            .document(id)
            .update(
                mapOf(
                    "name" to newName,
                    "priority" to newPriority,
                    "colorArgb" to newColorArgb
                )
            )
            .await()

        android.util.Log.e("SubjectFirestore", "수정 완료 id: $id")
    }

    suspend fun deleteSubject(userId: String, id: String) {
        if (id.isBlank()) {
            android.util.Log.e("SubjectFirestore", "삭제 중단: id가 비어 있음")
            return
        }

        db.collection("users")
            .document(userId)
            .collection("subjects")
            .document(id)
            .delete()
            .await()
    }

    suspend fun addSubject(
        userId: String,
        name: String,
        priority: Int,
        colorArgb: Int
    ) {
        val docRef = db.collection("users")
            .document(userId)
            .collection("subjects")
            .document() // 🔥 id 생성

        val subjectData = hashMapOf(
            "id" to docRef.id, // 🔥 핵심
            "name" to name,
            "priority" to priority,
            "colorArgb" to colorArgb
        )

        docRef.set(subjectData).await()
    }

    suspend fun getSubjects(userId: String): List<SubjectItem> {
        val result = db.collection("users")
            .document(userId)
            .collection("subjects")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            val priority = doc.getLong("priority")?.toInt() ?: 1
            val colorArgb = doc.getLong("colorArgb")?.toInt() ?: 0

            SubjectItem(
                id = doc.id, // 🔥 핵심
                name = name,
                priority = priority,
                colorArgb = colorArgb
            )
        }
    }
}