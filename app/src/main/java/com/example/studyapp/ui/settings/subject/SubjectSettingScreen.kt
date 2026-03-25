package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SubjectSettingScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel
) {
    var subjectName by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    val subjectColors = listOf(
        Color(0xFFFFC1CC),
        Color(0xFFFFD6A5),
        Color(0xFFFFF1A8),
        Color(0xFFCDEAC0),
        Color(0xFFA8E6E1),
        Color(0xFFBDE0FE),
        Color(0xFFD9C2F0),
        Color(0xFFE5D4C0)
    )

    var selectedColorArgb by remember {
        mutableIntStateOf(subjectColors.first().toArgb())
    }

    var editingSubjectName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
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

        Text(
            text = "중요도",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        MoonPrioritySelector(
            selectedPriority = priority,
            onPrioritySelected = { selectedPriority ->
                priority = selectedPriority
            }
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = "과목 색상",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        SubjectColorPicker(
            colors = subjectColors,
            selectedColorArgb = selectedColorArgb,
            onColorSelected = { color ->
                selectedColorArgb = color.toArgb()
            }
        )

        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = {
                val success = if (editingSubjectName == null) {
                    subjectViewModel.addSubject(
                        name = subjectName,
                        priority = priority,
                        colorArgb = selectedColorArgb
                    )
                } else {
                    subjectViewModel.updateSubject(
                        oldName = editingSubjectName!!,
                        newName = subjectName,
                        newPriority = priority,
                        newColorArgb = selectedColorArgb
                    )
                }

                if (success) {
                    subjectName = ""
                    priority = 1
                    selectedColorArgb = subjectColors.first().toArgb()
                    editingSubjectName = null
                }
            }
        ) {
            Text(
                text = if (editingSubjectName == null) "저장" else "수정 완료"
            )
        }

        subjectViewModel.subjects.forEach { subject ->
            SubjectItemRow(
                subject = subject,
                onEdit = {
                    subjectName = subject.name
                    priority = subject.priority
                    selectedColorArgb = subject.colorArgb
                    editingSubjectName = subject.name
                },
                onDelete = {
                    subjectViewModel.removeSubject(subject.name)

                    if (editingSubjectName == subject.name) {
                        subjectName = ""
                        priority = 1
                        selectedColorArgb = subjectColors.first().toArgb()
                        editingSubjectName = null
                    }
                }
            )
        }
    }
}