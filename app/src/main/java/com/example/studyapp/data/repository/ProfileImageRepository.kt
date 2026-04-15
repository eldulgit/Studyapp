package com.example.studyapp.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfileImageRepository {

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String {
        val ref = storage.reference.child("profile_images/$uid")

        ref.putFile(imageUri).await()

        return ref.downloadUrl.await().toString()
    }
}