package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjectsFromFirestore()
    }

    var subjectName by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    val subjectColors = listOf(
        Color(0xFFFDE2E4),
        Color(0xFFF8C8DC),
        Color(0xFFFFC1CC),
        Color(0xFFFFD6A5),
        Color(0xFFFFE5B4),
        Color(0xFFFFECB3),
        Color(0xFFE2F0CB),
        Color(0xFFCDEAC0),
        Color(0xFFD6F5E3),
        Color(0xFFA8E6E1),
        Color(0xFFBDE0FE),
        Color(0xFFD0E6FF),
        Color(0xFFBFCBFF),
        Color(0xFFD9C2F0),
        Color(0xFFEADCF8)
    )

    var selectedColorArgb by remember {
        mutableIntStateOf(subjectColors.first().toArgb())
    }

    // 이름 대신 id를 들고 감
    var editingSubjectId by remember { mutableStateOf<String?>(null) }

    val subjectListScrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
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
                    contentDescription = "뒤로가기"
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
                val trimmedName = subjectName.trim()
                if (trimmedName.isBlank()) return@Button

                val success = if (editingSubjectId == null) {
                    val result = subjectViewModel.addSubject(
                        name = trimmedName,
                        priority = priority,
                        colorArgb = selectedColorArgb
                    )

                    if (result) {
                        subjectViewModel.addSubjectToFirestore(
                            name = trimmedName,
                            priority = priority,
                            colorArgb = selectedColorArgb
                        )
                    }

                    result
                } else {
                    val oldSubject = subjectViewModel.subjects.find { it.id == editingSubjectId }
                        ?: return@Button

                    val result = subjectViewModel.updateSubject(
                        oldName = oldSubject.name,
                        newName = trimmedName,
                        newPriority = priority,
                        newColorArgb = selectedColorArgb
                    )

                    if (result && oldSubject.id.isNotBlank()) {
                        subjectViewModel.updateSubjectInFirestore(
                            id = oldSubject.id,
                            newName = trimmedName,
                            newPriority = priority,
                            newColorArgb = selectedColorArgb
                        )
                    }

                    result
                }

                if (success) {
                    subjectName = ""
                    priority = 1
                    selectedColorArgb = subjectColors.first().toArgb()
                    editingSubjectId = null

                    // 🔥 중요: Firestore 기준으로 다시 동기화
                    //subjectViewModel.loadSubjectsFromFirestore()
                }
            }
        ) {
            Text(text = if (editingSubjectId == null) "저장" else "수정 완료")
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(subjectListScrollState)
                .padding(bottom = 24.dp)
        ) {
            subjectViewModel.subjects.forEach { subject ->
                SubjectItemRow(
                    subject = subject,
                    onEdit = {
                        subjectName = subject.name
                        priority = subject.priority
                        selectedColorArgb = subject.colorArgb
                        editingSubjectId = subject.id
                    },
                    onDelete = {
                        subjectViewModel.removeSubject(subject.id)
                        subjectViewModel.removeSubjectFromFirestore(subject.id)

                        if (editingSubjectId == subject.id) {
                            subjectName = ""
                            priority = 1
                            selectedColorArgb = subjectColors.first().toArgb()
                            editingSubjectId = null
                        }
                    }
                )
            }
        }
    }
}