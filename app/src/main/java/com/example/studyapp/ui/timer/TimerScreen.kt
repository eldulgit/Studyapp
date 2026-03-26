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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.settings.subject.SubjectViewModel
import com.example.studyapp.ui.timer.pomodoro.CircularTimer
import com.example.studyapp.ui.timer.pomodoro.buildSingleSubjectSegment
import com.example.studyapp.ui.timer.pomodoro.formatCountdown
import com.example.studyapp.ui.timer.pomodoro.formatHoursMinutes

@Composable
fun TimerScreen(
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel
) {
    val availableSubjects = subjectViewModel.subjects
    val timerSubjects = timerViewModel.subjects

    var showSubjectDialog by remember { mutableStateOf(false) }
    var checkedSubjects by remember { mutableStateOf(setOf<String>()) }

    var showTimeEditDialog by remember { mutableStateOf(false) }
    var editTargetId by remember { mutableLongStateOf(-1L) }

    val timerWidth = 270.dp

    val currentEditTarget = timerSubjects.firstOrNull { it.id == editTargetId }

    val selectedTask = timerSubjects.firstOrNull { it.id == timerViewModel.selectedTaskId }

    val segments = if (selectedTask == null) {
        emptyList()
    } else {
        buildSingleSubjectSegment(
            allocatedSeconds = selectedTask.allocatedSeconds,
            remainingSeconds = selectedTask.remainingSeconds,
            taskId = selectedTask.id,
            label = selectedTask.name
        )
    }

    val runningTaskColor = availableSubjects
        .firstOrNull { it.name == selectedTask?.name }
        ?.let { Color(it.colorArgb) }
        ?: Color(0xFF4CAF50)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubjectDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "과목 추가"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.size(270.dp)) {
                CircularTimer(
                    modifier = Modifier.fillMaxSize(),
                    segments = segments,
                    colorForIndex = { runningTaskColor }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .width(timerWidth)
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = timerSubjects,
                    key = { it.id }
                ) { item ->
                    val isRunning = timerViewModel.runningTaskId == item.id

                    val subjectColorArgb = availableSubjects
                        .firstOrNull { it.name == item.name }
                        ?.colorArgb
                        ?: Color.Gray.toArgb()

                    TimerTaskRow(
                        subject = item.name,
                        time = formatCountdown(item.remainingSeconds),
                        subjectColorArgb = subjectColorArgb,
                        containerWidth = timerWidth,
                        isRunning = isRunning,
                        onToggle = {
                            timerViewModel.toggleTask(item.id)
                        },
                        onEditClick = {
                            editTargetId = item.id
                            showTimeEditDialog = true
                        }
                    )
                }
            }
        }
    }

    TimeEditDialog(
        show = showTimeEditDialog,
        initialTime = currentEditTarget?.let {
            formatHoursMinutes(it.allocatedSeconds)
        } ?: "0H 0M",
        onDismiss = {
            showTimeEditDialog = false
            editTargetId = -1L
        },
        onSave = { hour, minute ->
            currentEditTarget?.let { subject ->
                timerViewModel.updateSubjectTime(subject.id, hour, minute)
            }
            showTimeEditDialog = false
            editTargetId = -1L
        }
    )

    SubjectSelectDialog(
        show = showSubjectDialog,
        availableSubjects = availableSubjects,
        checkedSubjects = checkedSubjects,
        onCheckedChange = { subjectName, checked ->
            checkedSubjects =
                if (checked) checkedSubjects + subjectName
                else checkedSubjects - subjectName
        },
        onDismiss = {
            checkedSubjects = emptySet()
            showSubjectDialog = false
        },
        onConfirm = {
            checkedSubjects.forEach { subjectName ->
                timerViewModel.addSubjectTimer(subjectName)
            }
            checkedSubjects = emptySet()
            showSubjectDialog = false
        }
    )
}