package com.example.studyapp.ui.settings.common

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.repository.NameRepository
import kotlinx.coroutines.launch

class NameViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NameRepository(application)

    var userName by mutableStateOf("name")
        private set

    init {
        viewModelScope.launch {
            repo.nameFlow.collect {
                userName = it
            }
        }
    }

    fun updateName(name: String) {
        userName = name
        viewModelScope.launch {
            repo.saveName(name)
        }
    }
}
