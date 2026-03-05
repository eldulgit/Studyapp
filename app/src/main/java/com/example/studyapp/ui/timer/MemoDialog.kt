package com.example.studyapp.ui.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun MemoDialog(
    show: Boolean,
    initialPage: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    if (!show) return

    var page by remember { mutableStateOf(initialPage) }
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("메모") },
        text = {
            Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("내용") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(page, note) }) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

