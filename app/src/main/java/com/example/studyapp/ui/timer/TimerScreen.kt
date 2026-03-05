package com.example.studyapp.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TimerScreen(navController: NavController, timerViewModel: TimerViewModel) {

    var isDialogOpen by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }

    var subjects by remember {
        mutableStateOf(
            listOf(
                SubjectTimer("국어", "2H 15M"),
                SubjectTimer("수학", "1H 0M")
            )
        )
    }

    val timerWidth = 270.dp

    var showMemo by remember { mutableStateOf(false) }
    var runningIndex by remember { mutableStateOf<Int?>(null) }
    var memoTargetIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { isDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally   // ⭐ 원래 중앙 정렬 복구
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Timer", style = MaterialTheme.typography.titleLarge)

                IconButton(onClick = { navController.navigate("Time Table") }) {
                    Icon(Icons.Outlined.ViewAgenda, contentDescription = null)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.size(270.dp)) {
                CircularTimer()
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.width(timerWidth),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(subjects) { index, item ->

                    val isRunning = runningIndex == index

                    TimerTaskRow(
                        subject = item.name,
                        time = item.time,
                        containerWidth = timerWidth,
                        isRunning = isRunning,
                        onToggle = {
                            runningIndex = if (isRunning) null else index
                        },
                        onMemoClick = {
                            memoTargetIndex = index
                            showMemo = true
                        }
                    )
                }
            }

            val currentMemo = memoTargetIndex?.let { subjects[it] }

            MemoDialog(
                show = showMemo,
                initialPage = currentMemo?.page ?: "",
                initialNote = currentMemo?.memo ?: "",
                onDismiss = { showMemo = false },
                onSave = { page, note ->
                    memoTargetIndex?.let { idx ->
                        subjects = subjects.toMutableList().also {
                            it[idx] = it[idx].copy(page = page, memo = note)
                        }
                    }
                    showMemo = false
                }
            )

        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text("Subject 추가") },
            text = {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    placeholder = { Text("과목을 입력하세요") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSubject.isNotBlank()) {
                        subjects = subjects + SubjectTimer(newSubject, "0H 0M")
                        newSubject = ""
                        isDialogOpen = false
                    }
                }) { Text("추가") }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) { Text("취소") }
            }
        )
    }
}
