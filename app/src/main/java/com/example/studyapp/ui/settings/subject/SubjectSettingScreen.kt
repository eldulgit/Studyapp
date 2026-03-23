package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SubjectSettingScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel
) {
    var subjectName by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    // null이면 추가 모드, 값이 있으면 수정 모드
    var editingSubjectName by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "과목 관리",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        SubjectInput(
            label = "과목명",
            value = subjectName,
            onValueChange = { subjectName = it }
        )

        MoonPrioritySelector(
            selectedPriority = priority,
            onPrioritySelected = { priority = it }
        )

        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = {
                if (subjectName.isNotBlank()) {
                    if (editingSubjectName == null) {
                        // 추가 모드
                        subjectViewModel.addSubject(subjectName, priority)
                    } else {
                        // 수정 모드
                        subjectViewModel.updateSubject(
                            oldName = editingSubjectName!!,
                            newName = subjectName,
                            newPriority = priority
                        )
                    }

                    // 입력창 초기화
                    subjectName = ""
                    priority = 1
                    editingSubjectName = null
                }
            }
        ) {
            Text(if (editingSubjectName == null) "저장" else "수정 완료")
        }

        subjectViewModel.subjects.forEach { subject ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${subject.first}  ${priorityToMoon(subject.second)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )

                IconButton(
                    onClick = {
                        subjectName = subject.first
                        priority = subject.second
                        editingSubjectName = subject.first
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정"
                    )
                }

                IconButton(
                    onClick = {
                        subjectViewModel.removeSubject(subject.first)

                        if (editingSubjectName == subject.first) {
                            subjectName = ""
                            priority = 1
                            editingSubjectName = null
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제"
                    )
                }
            }
        }
    }
}

fun priorityToMoon(priority: Int): String {
    return when (priority) {
        1 -> "🌙"
        2 -> "🌗"
        3 -> "🌕"
        else -> "🌙"
    }
}

@Composable
fun MoonPrioritySelector(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(1, 2, 3).forEach { priority ->
            val isSelected = selectedPriority == priority

            Button(
                onClick = { onPrioritySelected(priority) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = priorityToMoon(priority),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}