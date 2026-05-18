package com.example.studyapp.data.repository

import com.example.studyapp.ui.settings.schedule.GoalItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GoalRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addGoal(
        userId: String,
        title: String,
        startDate: String,
        endDate: String,
        increasePriorityOverTime: Boolean = false
    ) {
        val docRef = db.collection("users")
            .document(userId)
            .collection("goals")
            .document()

        val goalData = hashMapOf(
            "id" to docRef.id,
            "title" to title,
            "startDate" to startDate,
            "endDate" to endDate,
            "pageCount" to 0,
            "increasePriorityOverTime" to increasePriorityOverTime
        )

        docRef.set(goalData).await()
    }

    suspend fun getGoals(userId: String): List<GoalItem> {
        val result = db.collection("users")
            .document(userId)
            .collection("goals")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val startDate = doc.getString("startDate") ?: return@mapNotNull null
            val endDate = doc.getString("endDate") ?: return@mapNotNull null
            val pageCount = doc.getLong("pageCount")?.toInt() ?: 0
            val increasePriorityOverTime =
                doc.getBoolean("increasePriorityOverTime") ?: false

            GoalItem(
                id = doc.id,
                title = title,
                startDate = startDate,
                endDate = endDate,
                pageCount = pageCount,
                increasePriorityOverTime = increasePriorityOverTime
            )
        }
    }

    suspend fun updateGoal(
        userId: String,
        id: String,
        title: String,
        startDate: String,
        endDate: String
    ) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document(id)
            .update(
                mapOf(
                    "title" to title,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "pageCount" to 0
                )
            )
            .await()
    }

    suspend fun deleteGoal(userId: String, id: String) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document(id)
            .delete()
            .await()
    }

    suspend fun updateGoalPriorityIncrease(
        userId: String,
        id: String,
        increasePriorityOverTime: Boolean
    ) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document(id)
            .update(
                "increasePriorityOverTime",
                increasePriorityOverTime
            )
            .await()
    }
}
