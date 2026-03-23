package com.example.studyapp.ui.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.studyapp.ui.settings.subject.priorityToMoon

@Composable
fun SubjectSelectDialog(
    show: Boolean,
    availableSubjects: List<Pair<String, Int>>,
    checkedSubjects: Set<String>,
    onCheckedChange: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("과목 선택")
        },
        text = {
            Column {
                if (availableSubjects.isEmpty()) {
                    Text("등록된 과목이 없습니다.")
                } else {
                    availableSubjects.forEach { subject ->
                        val name = subject.first
                        val priority = subject.second
                        val isChecked = checkedSubjects.contains(name)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    onCheckedChange(name, checked)
                                }
                            )

                            Text("$name - 중요도 ${priorityToMoon(priority)}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}