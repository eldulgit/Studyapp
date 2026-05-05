package com.example.studyapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.studyapp.data.dataStore
import kotlinx.coroutines.flow.map

class NameRepository(context: Context) {

    private val dataStore = context.dataStore

    suspend fun saveName(name: String) {
        dataStore.edit {
            it[stringPreferencesKey("user_name")] = name
        }
    }

    val nameFlow = dataStore.data.map {
        it[stringPreferencesKey("user_name")] ?: "name"
    }
}
