package com.example.studyapp.data.repository

import android.content.Context
import android.net.Uri
import java.io.File

class ProfileImageRepository {

    fun getSavedProfileImageUri(context: Context): String {
        val file = getProfileImageFile(context)
        return if (file.exists()) file.toVersionedUriString() else ""
    }

    fun saveProfileImage(context: Context, imageUri: Uri): String {
        val outputFile = getProfileImageFile(context)
        outputFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(imageUri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Profile image uri cannot be opened.")

        return outputFile.toVersionedUriString()
    }

    private fun getProfileImageFile(context: Context): File {
        return File(context.filesDir, "profile/profile_image.jpg")
    }

    private fun File.toVersionedUriString(): String {
        return Uri.fromFile(this)
            .buildUpon()
            .appendQueryParameter("updated", lastModified().toString())
            .build()
            .toString()
    }
}
