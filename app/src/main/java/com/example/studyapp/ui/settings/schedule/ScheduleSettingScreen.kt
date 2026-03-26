package com.example.studyapp.ui.settings.schedule

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleSettingScreen(
    navController: NavController
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ScheduleCategory.GOAL) }
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf("") }
    val dayOptions = listOf("월", "화", "수", "목", "금", "토", "일")
    var selectedDay by remember { mutableStateOf("월") }
    var isDayDropdownExpanded by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItemId by remember { mutableStateOf<Long?>(null) }
    var fixedScheduleList by remember {
        mutableStateOf<List<FixedScheduleItem>>(emptyList())
    }

    // CustomTimePicker 제어용 상태
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var isSelectingStartTime by remember { mutableStateOf(true) }
    var pendingStartTime by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = String.format(
                    Locale.getDefault(),
                    "%04d.%02d.%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    fun parseHour(time: String): Int {
        return time.split(":").getOrNull(0)?.toIntOrNull() ?: 0
    }

    fun parseMinute(time: String): Int {
        return time.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    }

    fun hasScheduleConflict(
        items: List<FixedScheduleItem>,
        editingId: Long?,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ): Boolean {
        val newStart = parseTimeToMinutes(startTime)
        val newEnd = parseTimeToMinutes(endTime)

        return items.any { item ->
            if (item.category != ScheduleCategory.SCHEDULE) return@any false
            if (item.dayOfWeek != dayOfWeek) return@any false
            if (editingId != null && item.id == editingId) return@any false

            val existingStart = item.startTime?.let { parseTimeToMinutes(it) } ?: return@any false
            val existingEnd = item.endTime?.let { parseTimeToMinutes(it) } ?: return@any false

            newStart < existingEnd && newEnd > existingStart
        }
    }

    val goalItems = fixedScheduleList.filter { it.category == ScheduleCategory.GOAL }
    val scheduleItems = fixedScheduleList.filter { it.category == ScheduleCategory.SCHEDULE }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingItemId = null
                    selectedCategory = ScheduleCategory.GOAL
                    title = ""
                    startDate = ""
                    endDate = ""
                    pageCount = ""
                    selectedDay = "월"
                    startTime = "09:00"
                    endTime = "10:00"
                    isDayDropdownExpanded = false
                    errorMessage = null

                    showTimePickerDialog = false
                    isSelectingStartTime = true
                    pendingStartTime = null

                    showAddDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "항목 추가"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "스케줄 설정",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = "스케줄",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ScheduleTimetable(
                                items = scheduleItems,
                                onItemClick = { item ->
                                    editingItemId = item.id
                                    selectedCategory = ScheduleCategory.SCHEDULE
                                    title = item.title
                                    startDate = item.startDate.orEmpty()
                                    endDate = item.endDate.orEmpty()
                                    pageCount = item.pageCount?.toString() ?: ""
                                    selectedDay = item.dayOfWeek ?: "월"
                                    startTime = item.startTime ?: "09:00"
                                    endTime = item.endTime ?: "10:00"
                                    isDayDropdownExpanded = false
                                    errorMessage = null

                                    showTimePickerDialog = false
                                    isSelectingStartTime = true
                                    pendingStartTime = null

                                    showAddDialog = true
                                }
                            )
                        }
                    }

                    item {
                        ScheduleSection(
                            title = "목표",
                            items = goalItems,
                            subtitleBuilder = {
                                "${it.startDate} ~ ${it.endDate} · ${it.pageCount ?: 0}p"
                            },
                            onEditClick = { item ->
                                editingItemId = item.id
                                selectedCategory = item.category
                                title = item.title
                                startDate = item.startDate.orEmpty()
                                endDate = item.endDate.orEmpty()
                                pageCount = item.pageCount?.toString() ?: ""
                                selectedDay = item.dayOfWeek ?: "월"
                                startTime = item.startTime ?: "09:00"
                                endTime = item.endTime ?: "10:00"
                                isDayDropdownExpanded = false
                                errorMessage = null

                                showTimePickerDialog = false
                                isSelectingStartTime = true
                                pendingStartTime = null

                                showAddDialog = true
                            }
                        )
                    }
                }

                if (showAddDialog) {
                    ScheduleAddDialog(
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        title = title,
                        onTitleChange = { title = it },
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateClick = {
                            showDatePicker { startDate = it }
                        },
                        onEndDateClick = {
                            showDatePicker { endDate = it }
                        },
                        pageCount = pageCount,
                        onPageCountChange = { pageCount = it },
                        dayOptions = dayOptions,
                        selectedDay = selectedDay,
                        onSelectedDayChange = { selectedDay = it },
                        isDayDropdownExpanded = isDayDropdownExpanded,
                        onDayDropdownExpandedChange = { isDayDropdownExpanded = it },
                        startTime = startTime,
                        endTime = endTime,
                        onStartTimeClick = {
                            errorMessage = null
                            isSelectingStartTime = true
                            pendingStartTime = null
                            showTimePickerDialog = true
                        },
                        onEndTimeClick = {
                            errorMessage = null
                            isSelectingStartTime = false
                            pendingStartTime = null
                            showTimePickerDialog = true
                        },
                        errorMessage = errorMessage,
                        onDismiss = {
                            showAddDialog = false
                            editingItemId = null
                            errorMessage = null
                            showTimePickerDialog = false
                            isSelectingStartTime = true
                            pendingStartTime = null
                        },
                        onConfirm = {
                            when {
                                title.isBlank() -> {
                                    errorMessage = "제목을 입력해주세요."
                                }

                                selectedCategory == ScheduleCategory.GOAL && startDate.isBlank() -> {
                                    errorMessage = "시작 날짜를 입력해주세요."
                                }

                                selectedCategory == ScheduleCategory.GOAL && endDate.isBlank() -> {
                                    errorMessage = "마감 날짜를 입력해주세요."
                                }

                                selectedCategory == ScheduleCategory.GOAL && pageCount.isBlank() -> {
                                    errorMessage = "페이지 수를 입력해주세요."
                                }

                                selectedCategory == ScheduleCategory.GOAL && startDate > endDate -> {
                                    errorMessage = "마감 날짜는 시작 날짜보다 빠를 수 없습니다."
                                }

                                selectedCategory == ScheduleCategory.SCHEDULE &&
                                        parseTimeToMinutes(startTime) >= parseTimeToMinutes(endTime) -> {
                                    errorMessage = "종료 시간은 시작 시간보다 늦어야 합니다."
                                }

                                selectedCategory == ScheduleCategory.SCHEDULE &&
                                        hasScheduleConflict(
                                            items = fixedScheduleList,
                                            editingId = editingItemId,
                                            dayOfWeek = selectedDay,
                                            startTime = startTime,
                                            endTime = endTime
                                        ) -> {
                                    errorMessage = "같은 요일에 시간이 겹치는 스케줄이 있습니다."
                                }

                                else -> {
                                    val newItem = if (selectedCategory == ScheduleCategory.GOAL) {
                                        FixedScheduleItem(
                                            id = editingItemId ?: System.currentTimeMillis(),
                                            category = ScheduleCategory.GOAL,
                                            title = title.trim(),
                                            startDate = startDate,
                                            endDate = endDate,
                                            pageCount = pageCount.toIntOrNull()
                                        )
                                    } else {
                                        FixedScheduleItem(
                                            id = editingItemId ?: System.currentTimeMillis(),
                                            category = ScheduleCategory.SCHEDULE,
                                            title = title.trim(),
                                            dayOfWeek = selectedDay,
                                            startTime = startTime,
                                            endTime = endTime
                                        )
                                    }

                                    fixedScheduleList = if (editingItemId == null) {
                                        fixedScheduleList + newItem
                                    } else {
                                        fixedScheduleList.map { oldItem ->
                                            if (oldItem.id == editingItemId) newItem else oldItem
                                        }
                                    }

                                    showAddDialog = false
                                    editingItemId = null
                                    errorMessage = null
                                    showTimePickerDialog = false
                                    isSelectingStartTime = true
                                    pendingStartTime = null
                                }
                            }
                        },
                        onDelete = if (editingItemId != null) {
                            {
                                fixedScheduleList = fixedScheduleList.filter { it.id != editingItemId }
                                showAddDialog = false
                                editingItemId = null
                                errorMessage = null
                                showTimePickerDialog = false
                                isSelectingStartTime = true
                                pendingStartTime = null
                            }
                        } else {
                            null
                        }
                    )
                }

                if (showTimePickerDialog) {
                    val initialTime = if (isSelectingStartTime) {
                        startTime
                    } else {
                        pendingStartTime ?: endTime
                    }

                    CustomTimePicker(
                        title = if (isSelectingStartTime) "시작시간" else "종료시간",
                        initialHour = parseHour(initialTime),
                        initialMinute = parseMinute(initialTime),
                        onDismiss = {
                            showTimePickerDialog = false
                            pendingStartTime = null
                            isSelectingStartTime = true
                        },
                        onConfirm = { selectedHour, selectedMinute ->
                            val selectedTime = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                selectedHour,
                                selectedMinute
                            )

                            if (isSelectingStartTime) {
                                startTime = selectedTime
                                endTime = selectedTime
                                pendingStartTime = selectedTime
                                errorMessage = null
                                isSelectingStartTime = false
                            } else {
                                val baseStartTime = pendingStartTime ?: startTime

                                if (parseTimeToMinutes(selectedTime) <= parseTimeToMinutes(baseStartTime)) {
                                    errorMessage = "종료 시간은 시작 시간보다 늦어야 합니다."
                                } else {
                                    startTime = baseStartTime
                                    endTime = selectedTime
                                    errorMessage = null
                                    showTimePickerDialog = false
                                    pendingStartTime = null
                                    isSelectingStartTime = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}