package com.example.studyapp.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.subject.SubjectItem

@Composable
fun SubjectSelectDialog(
    show: Boolean,
    availableSubjects: List<SubjectItem>,
    checkedSubjects: Set<String>,
    onCheckedChange: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("과목 선택") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSubjects.forEach { subject ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedSubjects.contains(subject.name),
                            onCheckedChange = { checked ->
                                onCheckedChange(subject.name, checked == true)
                            }
                        )

                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(subject.colorArgb), CircleShape)
                        )

                        Text(
                            text = " ${subject.name}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}