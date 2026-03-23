package com.example.studyapp.ui.settings.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleAddDialog(
    selectedCategory: ScheduleCategory,
    onCategoryChange: (ScheduleCategory) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,

    startDate: String,
    endDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,

    pageCount: String,
    onPageCountChange: (String) -> Unit,

    dayOptions: List<String>,
    selectedDay: String,
    onSelectedDayChange: (String) -> Unit,
    isDayDropdownExpanded: Boolean,
    onDayDropdownExpandedChange: (Boolean) -> Unit,

    startTime: String,
    endTime: String,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,

    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "카테고리",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCategory == ScheduleCategory.GOAL,
                        onClick = { onCategoryChange(ScheduleCategory.GOAL) }
                    )
                    Text(
                        text = "목표",
                        modifier = Modifier.clickable {
                            onCategoryChange(ScheduleCategory.GOAL)
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = selectedCategory == ScheduleCategory.SCHEDULE,
                        onClick = { onCategoryChange(ScheduleCategory.SCHEDULE) }
                    )
                    Text(
                        text = "스케줄",
                        modifier = Modifier.clickable {
                            onCategoryChange(ScheduleCategory.SCHEDULE)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "설정",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedCategory == ScheduleCategory.GOAL) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("시작 날짜") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { onStartDateClick() }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("마감 날짜") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { onEndDateClick() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pageCount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                onPageCountChange(input)
                            }
                        },
                        label = { Text("페이지/인강 수") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("요일") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { onDayDropdownExpandedChange(true) }
                        )

                        DropdownMenu(
                            expanded = isDayDropdownExpanded,
                            onDismissRequest = { onDayDropdownExpandedChange(false) }
                        ) {
                            dayOptions.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        onSelectedDayChange(day)
                                        onDayDropdownExpandedChange(false)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("시작 시간") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { onStartTimeClick() }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("종료 시간") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { onEndTimeClick() }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onConfirm) {
                        Text("확인")
                    }
                }
            }
        }
    }
}