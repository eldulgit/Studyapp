package com.example.studyapp.ui.settings.schedule

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyapp.ui.theme.isAppInDarkTheme
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
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

    dayOptions: List<String>,
    selectedDay: String,
    onSelectedDayChange: (String) -> Unit,
    isDayDropdownExpanded: Boolean,
    onDayDropdownExpandedChange: (Boolean) -> Unit,

    startTime: String,
    endTime: String,
    scheduleTimeInputs: List<ScheduleTimeInput>,
    isEditingSchedule: Boolean,
    onScheduleTimeDayChange: (Int, String) -> Unit,
    onAddScheduleTime: () -> Unit,
    onRemoveScheduleTime: (Int) -> Unit,
    onScheduleStartTimeClick: (Int) -> Unit,
    onScheduleEndTimeClick: (Int) -> Unit,

    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)? = null,   // 추가
    errorMessage: String?,
) {
    val dateValidationError =
        if (
            selectedCategory == ScheduleCategory.GOAL &&
            startDate.isNotBlank() &&
            endDate.isNotBlank()
        ) {
            val start = runCatching { LocalDate.parse(startDate) }.getOrNull()
            val end = runCatching { LocalDate.parse(endDate) }.getOrNull()

            if (start != null && end != null && !end.isAfter(start)) {
                "마감 날짜는 시작 날짜보다 늦은 날짜여야 합니다."
            } else {
                null
            }
        } else {
            null
    }

    val displayErrorMessage = dateValidationError ?: errorMessage
    val isDarkTheme = isAppInDarkTheme()
    val dialogSurfaceColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFF1F5F9)
    }
    val inputSurfaceColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.White
    }
    var dropdownWidth by remember { mutableStateOf(0.dp) }
    var expandedScheduleIndex by remember {
        mutableStateOf<Int?>(null)
    }
    val scheduleInputScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = dialogSurfaceColor,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "카테고리",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            onCategoryChange(ScheduleCategory.GOAL)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == ScheduleCategory.GOAL,
                            onClick = { onCategoryChange(ScheduleCategory.GOAL) }
                        )
                        Text(
                            text = "목표",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier.clickable {
                            onCategoryChange(ScheduleCategory.SCHEDULE)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == ScheduleCategory.SCHEDULE,
                            onClick = { onCategoryChange(ScheduleCategory.SCHEDULE) }
                        )
                        Text(
                            text = "시간표",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("제목") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .align(Alignment.CenterHorizontally),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputSurfaceColor,
                        unfocusedContainerColor = inputSurfaceColor,
                        disabledContainerColor = inputSurfaceColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedCategory == ScheduleCategory.GOAL) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InlineScheduleValue(
                            text = formatMonthDayOrPlaceholder(startDate, "시작 날짜"),
                            modifier = Modifier.weight(1f),
                            containerColor = inputSurfaceColor,
                            onClick = onStartDateClick
                        )

                        InlineScheduleValue(
                            text = formatMonthDayOrPlaceholder(endDate, "마감 날짜"),
                            modifier = Modifier.weight(1f),
                            containerColor = inputSurfaceColor,
                            onClick = onEndDateClick
                        )
                    }
                }

                if (selectedCategory == ScheduleCategory.GOAL) {
                    Text(
                        text = "과목과 다른 이름을 사용해주세요.",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = selectedCategory == ScheduleCategory.SCHEDULE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 156.dp)
                                .verticalScroll(scheduleInputScrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            scheduleTimeInputs.forEachIndexed { index, input ->
                                val canRemoveScheduleTime =
                                    scheduleTimeInputs.size > 1

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            InlineScheduleValue(
                                                text = input.dayOfWeek,
                                                modifier = Modifier.fillMaxWidth(),
                                                containerColor = inputSurfaceColor,
                                                onClick = {
                                                    expandedScheduleIndex = index
                                                    onDayDropdownExpandedChange(true)
                                                },
                                                onWidthChanged = { dropdownWidth = it }
                                            )

                                            DropdownMenu(
                                                expanded = isDayDropdownExpanded &&
                                                        expandedScheduleIndex == index,
                                                modifier = Modifier.width(dropdownWidth),
                                                onDismissRequest = {
                                                    expandedScheduleIndex = null
                                                    onDayDropdownExpandedChange(false)
                                                },
                                                containerColor = dialogSurfaceColor
                                            ) {
                                                dayOptions.forEach { day ->
                                                    DropdownMenuItem(
                                                        text = { Text(day) },
                                                        onClick = {
                                                            onSelectedDayChange(day)
                                                            onScheduleTimeDayChange(index, day)
                                                            expandedScheduleIndex = null
                                                            onDayDropdownExpandedChange(false)
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            InlineScheduleValue(
                                                text = input.startTime,
                                                modifier = Modifier.fillMaxWidth(),
                                                containerColor = inputSurfaceColor,
                                                onClick = { onScheduleStartTimeClick(index) }
                                            )
                                        }

                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            InlineScheduleValue(
                                                text = input.endTime,
                                                modifier = Modifier.fillMaxWidth(),
                                                containerColor = inputSurfaceColor,
                                                onClick = { onScheduleEndTimeClick(index) }
                                            )
                                        }

                                        Box(
                                            modifier = Modifier.width(
                                                if (canRemoveScheduleTime) 32.dp else 0.dp
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (canRemoveScheduleTime) {
                                                IconButton(
                                                    onClick = { onRemoveScheduleTime(index) },
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "시간 삭제"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onAddScheduleTime,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = inputSurfaceColor,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "요일/시간 추가",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                if (displayErrorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = displayErrorMessage,
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

                    TextButton(
                        onClick = {
                            if (dateValidationError == null) {
                                onConfirm()
                            }
                        },
                        enabled = dateValidationError == null
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatMonthDayOrPlaceholder(date: String, placeholder: String): String {
    if (date.isBlank()) return placeholder

    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return date
    return "${parsedDate.monthValue}월${parsedDate.dayOfMonth}일"
}

@Composable
private fun InlineScheduleValue(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit,
    onWidthChanged: ((Dp) -> Unit)? = null
) {
    val density = LocalDensity.current

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .onGloballyPositioned { coordinates ->
                onWidthChanged?.invoke(
                    with(density) { coordinates.size.width.toDp() }
                )
            }
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
