package com.example.studyapp.ui.settings.lifestyle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LifeStyleSettingScreen(
    initialWakeTime: String = "",
    initialSleepTime: String = "",
    initialLunchStartTime: String = "",
    initialLunchEndTime: String = "",
    initialDinnerStartTime: String = "",
    initialDinnerEndTime: String = "",
    onSaveClick: (
        wakeTime: String,
        sleepTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        dinnerStartTime: String,
        dinnerEndTime: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var wakeTime by remember { mutableStateOf(initialWakeTime) }
    var sleepTime by remember { mutableStateOf(initialSleepTime) }
    var lunchStartTime by remember { mutableStateOf(initialLunchStartTime) }
    var lunchEndTime by remember { mutableStateOf(initialLunchEndTime) }
    var dinnerStartTime by remember { mutableStateOf(initialDinnerStartTime) }
    var dinnerEndTime by remember { mutableStateOf(initialDinnerEndTime) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        initialWakeTime,
        initialSleepTime,
        initialLunchStartTime,
        initialLunchEndTime,
        initialDinnerStartTime,
        initialDinnerEndTime
    ) {
        wakeTime = initialWakeTime
        sleepTime = initialSleepTime
        lunchStartTime = initialLunchStartTime
        lunchEndTime = initialLunchEndTime
        dinnerStartTime = initialDinnerStartTime
        dinnerEndTime = initialDinnerEndTime
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "생활패턴 설정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "학습 계획에 반영될 생활패턴을 수정할 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = wakeTime,
            onValueChange = {
                wakeTime = it
                errorMessage = null
            },
            label = { Text("기상 시간") },
            placeholder = { Text("예: 07:00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sleepTime,
            onValueChange = {
                sleepTime = it
                errorMessage = null
            },
            label = { Text("취침 시간") },
            placeholder = { Text("예: 23:30") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "점심 시간",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = lunchStartTime,
                onValueChange = {
                    lunchStartTime = it
                    errorMessage = null
                },
                label = { Text("시작") },
                placeholder = { Text("12:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            OutlinedTextField(
                value = lunchEndTime,
                onValueChange = {
                    lunchEndTime = it
                    errorMessage = null
                },
                label = { Text("끝") },
                placeholder = { Text("13:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "저녁 시간",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = dinnerStartTime,
                onValueChange = {
                    dinnerStartTime = it
                    errorMessage = null
                },
                label = { Text("시작") },
                placeholder = { Text("18:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            OutlinedTextField(
                value = dinnerEndTime,
                onValueChange = {
                    dinnerEndTime = it
                    errorMessage = null
                },
                label = { Text("끝") },
                placeholder = { Text("19:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (
                    wakeTime.isBlank() ||
                    sleepTime.isBlank() ||
                    lunchStartTime.isBlank() ||
                    lunchEndTime.isBlank() ||
                    dinnerStartTime.isBlank() ||
                    dinnerEndTime.isBlank()
                ) {
                    errorMessage = "기상 시간, 취침 시간, 점심 시간, 저녁 시간을 모두 입력해주세요."
                    return@Button
                }

                onSaveClick(
                    wakeTime,
                    sleepTime,
                    lunchStartTime,
                    lunchEndTime,
                    dinnerStartTime,
                    dinnerEndTime
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "저장")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}