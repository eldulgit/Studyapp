package com.example.studyapp.ui.settings.schedule

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ScheduleTimeInput(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleSettingScreen() {
    val scheduleViewModel: ScheduleViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val goalViewModel: GoalViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        scheduleViewModel.loadSchedulesFromFirestore()
        goalViewModel.loadGoalsFromFirestore()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ScheduleCategory.GOAL) }
    val listState = rememberLazyListState()

    val showFab by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isAtTop = listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0

            isAtTop || visibleItems.isEmpty() || visibleItems.none { it.index >= 1 }
        }
    }
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val dayOptions = listOf("월", "화", "수", "목", "금", "토", "일")

    var selectedDay by remember { mutableStateOf("월") }
    var isDayDropdownExpanded by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var scheduleTimeInputs by remember {
        mutableStateOf(listOf(ScheduleTimeInput("월", "09:00", "10:00")))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItemId by remember { mutableStateOf<Long?>(null) }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var isSelectingStartTime by remember { mutableStateOf(true) }
    var pendingStartTime by remember { mutableStateOf<String?>(null) }
    var editingTimeInputIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun showDatePicker(
        initialDate: String,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        if (initialDate.isNotBlank()) {
            val parts = initialDate.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull()
                val month = parts[1].toIntOrNull()
                val day = parts[2].toIntOrNull()

                if (year != null && month != null && day != null) {
                    calendar.set(year, month - 1, day)
                }
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = "%04d-%02d-%02d".format(
                    year,
                    month + 1,
                    dayOfMonth
                )
                onDateSelected(selectedDate)
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
        return time
            .split(":")
            .getOrNull(0)
            ?.toIntOrNull()
            ?: 9
    }

    fun parseMinute(time: String): Int {
        return time
            .split(":")
            .getOrNull(1)
            ?.toIntOrNull()
            ?: 0
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

    fun updateScheduleTimeInput(
        index: Int,
        transform: (ScheduleTimeInput) -> ScheduleTimeInput
    ) {
        scheduleTimeInputs = scheduleTimeInputs.mapIndexed { itemIndex, item ->
            if (itemIndex == index) transform(item) else item
        }
    }

    fun scheduleInputsHaveInternalConflict(inputs: List<ScheduleTimeInput>): Boolean {
        return inputs.withIndex().any { (index, input) ->
            inputs.withIndex().any { (otherIndex, other) ->
                index < otherIndex &&
                        input.dayOfWeek == other.dayOfWeek &&
                        parseTimeToMinutes(input.startTime) < parseTimeToMinutes(other.endTime) &&
                        parseTimeToMinutes(input.endTime) > parseTimeToMinutes(other.startTime)
            }
        }
    }

    val goalItems = goalViewModel.goals.map { goal ->
        FixedScheduleItem(
            id = goal.id.hashCode().toLong(),
            firestoreId = goal.id,
            category = ScheduleCategory.GOAL,
            title = goal.title,
            startDate = goal.startDate,
            endDate = goal.endDate,
            increasePriorityOverTime = goal.increasePriorityOverTime
        )
    }

    val scheduleItems = scheduleViewModel.schedules.map {
        FixedScheduleItem(
            id = it.id.hashCode().toLong(),
            category = ScheduleCategory.SCHEDULE,
            title = it.title,
            dayOfWeek = it.dayOfWeek,
            startTime = it.startTime,
            endTime = it.endTime
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            if (showFab && !showAddDialog && !showTimePickerDialog) {
                FloatingActionButton(
                    onClick = {
                        editingItemId = null
                        selectedCategory = ScheduleCategory.GOAL
                        title = ""
                        startDate = ""
                        endDate = ""
                        selectedDay = "월"
                        startTime = "09:00"
                        endTime = "10:00"
                        scheduleTimeInputs = listOf(ScheduleTimeInput("월", "09:00", "10:00"))
                        isDayDropdownExpanded = false
                        errorMessage = null
                        showTimePickerDialog = false
                        isSelectingStartTime = true
                        pendingStartTime = null
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "항목 추가"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                state = listState,
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
                                selectedDay = item.dayOfWeek ?: "월"
                                startTime = item.startTime ?: "09:00"
                                endTime = item.endTime ?: "10:00"
                                scheduleTimeInputs = listOf(
                                    ScheduleTimeInput(
                                        dayOfWeek = item.dayOfWeek ?: "월",
                                        startTime = item.startTime ?: "09:00",
                                        endTime = item.endTime ?: "10:00"
                                    )
                                )
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
                        guideText = "체크하면 우선순위가 자동 상승해요",
                        items = goalItems,
                        subtitleBuilder = {
                            "${it.startDate} ~ ${it.endDate}"
                        },
                        onCheckedChange = { item, checked ->
                            val goalId = item.firestoreId
                            if (goalId != null) {
                                goalViewModel.updateGoalPriorityIncrease(
                                    id = goalId,
                                    increasePriorityOverTime = checked
                                )
                            }
                        },
                        onEditClick = { item ->
                            editingItemId = item.id
                            selectedCategory = item.category
                            title = item.title
                            startDate = item.startDate.orEmpty()
                            endDate = item.endDate.orEmpty()
                            selectedDay = item.dayOfWeek ?: "월"
                            startTime = item.startTime ?: "09:00"
                            endTime = item.endTime ?: "10:00"
                            scheduleTimeInputs = listOf(
                                ScheduleTimeInput(
                                    dayOfWeek = item.dayOfWeek ?: "월",
                                    startTime = item.startTime ?: "09:00",
                                    endTime = item.endTime ?: "10:00"
                                )
                            )
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
                        showDatePicker(endDate.ifBlank { startDate }) { selectedDate ->
                            startDate = selectedDate

                            if (endDate.isNotBlank() && endDate <= selectedDate) {
                                endDate = ""
                                errorMessage = "마감 날짜는 시작 날짜보다 늦은 날짜여야 합니다."
                            } else {
                                errorMessage = null
                            }
                        }
                    },
                    onEndDateClick = {
                        showDatePicker(endDate.ifBlank { startDate }) { selectedDate ->
                            if (startDate.isBlank()) {
                                errorMessage = "시작 날짜를 먼저 선택해주세요."
                                return@showDatePicker
                            }

                            if (selectedDate <= startDate) {
                                errorMessage = "마감 날짜는 시작 날짜보다 늦은 날짜여야 합니다."
                                return@showDatePicker
                            }

                            endDate = selectedDate
                            errorMessage = null
                        }
                    },
                    dayOptions = dayOptions,
                    selectedDay = selectedDay,
                    onSelectedDayChange = { selectedDay = it },
                    isDayDropdownExpanded = isDayDropdownExpanded,
                    onDayDropdownExpandedChange = { isDayDropdownExpanded = it },
                    startTime = startTime,
                    endTime = endTime,
                    scheduleTimeInputs = scheduleTimeInputs,
                    isEditingSchedule = editingItemId != null &&
                            selectedCategory == ScheduleCategory.SCHEDULE,
                    onScheduleTimeDayChange = { index, day ->
                        updateScheduleTimeInput(index) { it.copy(dayOfWeek = day) }
                        selectedDay = day
                    },
                    onAddScheduleTime = {
                        scheduleTimeInputs = scheduleTimeInputs +
                                ScheduleTimeInput("월", "09:00", "10:00")
                    },
                    onRemoveScheduleTime = { index ->
                        scheduleTimeInputs = scheduleTimeInputs
                            .filterIndexed { itemIndex, _ -> itemIndex != index }
                            .ifEmpty { listOf(ScheduleTimeInput("월", "09:00", "10:00")) }
                    },
                    onScheduleStartTimeClick = { index ->
                        errorMessage = null
                        editingTimeInputIndex = index
                        isSelectingStartTime = true
                        pendingStartTime = null
                        showTimePickerDialog = true
                    },
                    onScheduleEndTimeClick = { index ->
                        errorMessage = null
                        editingTimeInputIndex = index
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

                            selectedCategory == ScheduleCategory.GOAL && startDate > endDate -> {
                                errorMessage = "마감 날짜는 시작 날짜보다 빠를 수 없습니다."
                            }

                            selectedCategory == ScheduleCategory.SCHEDULE &&
                                    scheduleTimeInputs.any {
                                        parseTimeToMinutes(it.startTime) >= parseTimeToMinutes(it.endTime)
                                    } -> {
                                errorMessage = "종료 시간은 시작 시간보다 늦어야 합니다."
                            }

                            selectedCategory == ScheduleCategory.SCHEDULE &&
                                    scheduleInputsHaveInternalConflict(scheduleTimeInputs) -> {
                                errorMessage = "추가하려는 스케줄끼리 시간이 겹칩니다."
                            }

                            selectedCategory == ScheduleCategory.SCHEDULE &&
                                    scheduleTimeInputs.any {
                                        hasScheduleConflict(
                                            items = scheduleItems,
                                            editingId = editingItemId,
                                            dayOfWeek = it.dayOfWeek,
                                            startTime = it.startTime,
                                            endTime = it.endTime
                                        )
                                    } -> {
                                errorMessage = "같은 요일에 시간이 겹치는 스케줄이 있습니다."
                            }

                            else -> {
                                if (selectedCategory == ScheduleCategory.GOAL) {
                                    if (editingItemId == null) {
                                        goalViewModel.addGoal(
                                            title = title.trim(),
                                            startDate = startDate,
                                            endDate = endDate
                                        )
                                    } else {
                                        val firestoreId = goalViewModel.goals
                                            .firstOrNull {
                                                it.id.hashCode().toLong() == editingItemId
                                            }
                                            ?.id

                                        if (firestoreId != null) {
                                            goalViewModel.updateGoal(
                                                id = firestoreId,
                                                title = title.trim(),
                                                startDate = startDate,
                                                endDate = endDate
                                            )
                                        }
                                    }
                                } else {
                                    val firstScheduleInput = scheduleTimeInputs.first()

                                    if (editingItemId == null) {
                                        scheduleViewModel.addSchedules(
                                            title = title.trim(),
                                            inputs = scheduleTimeInputs
                                        )
                                    } else {
                                        val firestoreId = scheduleViewModel.schedules
                                            .firstOrNull {
                                                it.id.hashCode().toLong() == editingItemId
                                            }
                                            ?.id

                                        if (firestoreId != null) {
                                            scheduleViewModel.updateSchedule(
                                                id = firestoreId,
                                                title = title.trim(),
                                                dayOfWeek = firstScheduleInput.dayOfWeek,
                                                startTime = firstScheduleInput.startTime,
                                                endTime = firstScheduleInput.endTime
                                            )

                                            val additionalScheduleInputs =
                                                scheduleTimeInputs.drop(1)
                                            if (additionalScheduleInputs.isNotEmpty()) {
                                                scheduleViewModel.addSchedules(
                                                    title = title.trim(),
                                                    inputs = additionalScheduleInputs
                                                )
                                            }
                                        }
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
                            if (selectedCategory == ScheduleCategory.GOAL) {
                                val firestoreId = goalViewModel.goals
                                    .firstOrNull { it.id.hashCode().toLong() == editingItemId }
                                    ?.id

                                if (firestoreId != null) {
                                    goalViewModel.deleteGoal(firestoreId)
                                }
                            } else {
                                val firestoreId = scheduleViewModel.schedules
                                    .firstOrNull { it.id.hashCode().toLong() == editingItemId }
                                    ?.id

                                if (firestoreId != null) {
                                    scheduleViewModel.deleteSchedule(firestoreId)
                                }
                            }

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
                val editingInput = scheduleTimeInputs.getOrNull(editingTimeInputIndex)
                    ?: ScheduleTimeInput("월", "09:00", "10:00")
                val initialTime = if (isSelectingStartTime) {
                    editingInput.startTime
                } else {
                    pendingStartTime ?: editingInput.endTime
                }

                CustomTimePicker(
                    title = if (isSelectingStartTime) "시작시간" else "종료시간",
                    initialHour = parseHour(initialTime),
                    initialMinute = parseMinute(initialTime),
                    blinkOnConfirm = isSelectingStartTime,
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
                            updateScheduleTimeInput(editingTimeInputIndex) {
                                it.copy(startTime = selectedTime, endTime = selectedTime)
                            }
                            pendingStartTime = selectedTime
                            errorMessage = null

                            coroutineScope.launch {
                                showTimePickerDialog = false
                                isSelectingStartTime = false
                                delay(30)
                                showTimePickerDialog = true
                            }
                        } else {
                            val baseStartTime = pendingStartTime ?: editingInput.startTime

                            if (parseTimeToMinutes(selectedTime) <= parseTimeToMinutes(baseStartTime)) {
                                errorMessage = "종료 시간은 시작 시간보다 늦어야 합니다."
                            } else {
                                startTime = baseStartTime
                                endTime = selectedTime
                                updateScheduleTimeInput(editingTimeInputIndex) {
                                    it.copy(startTime = baseStartTime, endTime = selectedTime)
                                }
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
