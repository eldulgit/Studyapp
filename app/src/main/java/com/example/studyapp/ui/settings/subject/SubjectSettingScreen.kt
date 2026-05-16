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
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
        Color(0xFFA8E6E1)
    )

    var selectedColorArgb by remember {
        mutableIntStateOf(subjectColors.first().toArgb())
    }

    var editingSubjectId by remember { mutableStateOf<String?>(null) }

    var subjectNameError by remember { mutableStateOf(false) }
    var subjectColorError by remember { mutableStateOf(false) }

    val screenScrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    /**
     * 현재 선택 불가능한 색상 목록.
     * 수정 중일 때는 자기 자신의 색상은 선택 가능해야 하므로 제외한다.
     */
    val disabledColorArgbList = subjectViewModel.subjects
        .filter { subject -> subject.id != editingSubjectId }
        .map { subject -> subject.colorArgb }

    /**
     * 화면 표시용 정렬 목록.
     * subjectColors에 정의된 색상 순서대로 정렬된다.
     */
    val sortedSubjects = subjectViewModel.subjects.sortedBy { subject ->
        val colorIndex = subjectColors.indexOfFirst { color ->
            color.toArgb() == subject.colorArgb
        }

        if (colorIndex == -1) Int.MAX_VALUE else colorIndex
    }

    fun getFirstAvailableColorArgb(): Int {
        return subjectColors.firstOrNull { color ->
            color.toArgb() !in subjectViewModel.subjects.map { subject -> subject.colorArgb }
        }?.toArgb() ?: subjectColors.first().toArgb()
    }

    fun resetInput() {
        subjectName = ""
        priority = 1
        editingSubjectId = null
        subjectNameError = false
        subjectColorError = false

        selectedColorArgb = getFirstAvailableColorArgb()
    }

    /**
     * Firestore에서 과목을 불러온 뒤,
     * 추가 모드에서 현재 선택 색상이 이미 사용 중이면
     * 사용 가능한 첫 번째 색상으로 자동 이동한다.
     */
    LaunchedEffect(disabledColorArgbList, editingSubjectId) {
        if (editingSubjectId == null && selectedColorArgb in disabledColorArgbList) {
            selectedColorArgb = getFirstAvailableColorArgb()
        }
    }

    fun saveSubject() {
        focusManager.clearFocus()
        keyboardController?.hide()

        val trimmedName = subjectName.trim()
        if (trimmedName.isBlank()) return

        val isDuplicateName = subjectViewModel.subjects.any { subject ->
            subject.name == trimmedName && subject.id != editingSubjectId
        }

        val isDuplicateColor = subjectViewModel.subjects.any { subject ->
            subject.colorArgb == selectedColorArgb && subject.id != editingSubjectId
        }

        subjectNameError = isDuplicateName
        subjectColorError = isDuplicateColor

        if (isDuplicateName || isDuplicateColor) return

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
                ?: return

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
            resetInput()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(screenScrollState)
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

            IconButton(
                onClick = { saveSubject() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = if (editingSubjectId == null) "저장" else "수정 완료"
                )
            }
        }

        SubjectInput(
            label = "과목명",
            value = subjectName,
            onValueChange = {
                subjectName = it
                subjectNameError = false
            }
        )

        if (subjectNameError) {
            Text(
                text = "이미 선택된 과목입니다.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

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
            disabledColorArgbList = disabledColorArgbList,
            onColorSelected = { color ->
                selectedColorArgb = color.toArgb()
                subjectColorError = false
            }
        )

        if (subjectColorError) {
            Text(
                text = "이미 선택된 색상입니다.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            sortedSubjects.forEach { subject ->
                SubjectItemRow(
                    subject = subject,
                    onEdit = {
                        subjectName = subject.name
                        priority = subject.priority
                        selectedColorArgb = subject.colorArgb
                        editingSubjectId = subject.id
                        subjectNameError = false
                        subjectColorError = false
                    },
                    onDelete = {
                        subjectViewModel.removeSubject(subject.id)
                        subjectViewModel.removeSubjectFromFirestore(subject.id)

                        if (editingSubjectId == subject.id) {
                            resetInput()
                        }
                    }
                )
            }
        }
    }
}