package com.example.studyapp.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studyapp.util.filterTimeInput
import com.example.studyapp.util.normalizeTimeInput

@Composable
fun LifestyleInputScreen(
    onCompleteClick: (
        wakeTime: String,
        sleepTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        dinnerStartTime: String,
        dinnerEndTime: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var wakeTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var lunchStartTime by remember { mutableStateOf("") }
    var lunchEndTime by remember { mutableStateOf("") }
    var dinnerStartTime by remember { mutableStateOf("") }
    var dinnerEndTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    val wakeFocusRequester = remember { FocusRequester() }
    val sleepFocusRequester = remember { FocusRequester() }
    val lunchStartFocusRequester = remember { FocusRequester() }
    val lunchEndFocusRequester = remember { FocusRequester() }
    val dinnerStartFocusRequester = remember { FocusRequester() }
    val dinnerEndFocusRequester = remember { FocusRequester() }

    fun updateTime(value: String, update: (String) -> Unit) {
        update(filterTimeInput(value))
        errorMessage = null
    }

    fun complete() {
        val normalizedWakeTime = normalizeTimeInput(wakeTime)
        val normalizedSleepTime = normalizeTimeInput(sleepTime)
        val normalizedLunchStartTime = normalizeTimeInput(lunchStartTime)
        val normalizedLunchEndTime = normalizeTimeInput(lunchEndTime)
        val normalizedDinnerStartTime = normalizeTimeInput(dinnerStartTime)
        val normalizedDinnerEndTime = normalizeTimeInput(dinnerEndTime)

        if (
            normalizedWakeTime == null ||
            normalizedSleepTime == null ||
            normalizedLunchStartTime == null ||
            normalizedLunchEndTime == null ||
            normalizedDinnerStartTime == null ||
            normalizedDinnerEndTime == null
        ) {
            errorMessage = "시간은 7:30, 730 형식으로 입력해주세요."
            return
        }

        wakeTime = normalizedWakeTime
        sleepTime = normalizedSleepTime
        lunchStartTime = normalizedLunchStartTime
        lunchEndTime = normalizedLunchEndTime
        dinnerStartTime = normalizedDinnerStartTime
        dinnerEndTime = normalizedDinnerEndTime

        onCompleteClick(
            normalizedWakeTime,
            normalizedSleepTime,
            normalizedLunchStartTime,
            normalizedLunchEndTime,
            normalizedDinnerStartTime,
            normalizedDinnerEndTime
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "생활패턴 입력",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "더 나은 학습 관리를 위해 기본 생활패턴을 입력해주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = wakeTime,
            onValueChange = { updateTime(it) { value -> wakeTime = value } },
            label = { Text("기상 시간") },
            placeholder = { Text("예: 7:30, 730") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { sleepFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(wakeFocusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sleepTime,
            onValueChange = { updateTime(it) { value -> sleepTime = value } },
            label = { Text("취침 시간") },
            placeholder = { Text("예: 7:30, 730") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { lunchStartFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(sleepFocusRequester)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "점심 시간",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = lunchStartTime,
                onValueChange = { updateTime(it) { value -> lunchStartTime = value } },
                label = { Text("시작") },
                placeholder = { Text("예: 7:30, 730") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { lunchEndFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(lunchStartFocusRequester)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            OutlinedTextField(
                value = lunchEndTime,
                onValueChange = { updateTime(it) { value -> lunchEndTime = value } },
                label = { Text("종료") },
                placeholder = { Text("예: 7:30, 730") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { dinnerStartFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(lunchEndFocusRequester)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "저녁 시간",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dinnerStartTime,
                onValueChange = { updateTime(it) { value -> dinnerStartTime = value } },
                label = { Text("시작") },
                placeholder = { Text("예: 7:30, 730") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { dinnerEndFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(dinnerStartFocusRequester)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            OutlinedTextField(
                value = dinnerEndTime,
                onValueChange = { updateTime(it) { value -> dinnerEndTime = value } },
                label = { Text("종료") },
                placeholder = { Text("예: 7:30, 730") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        complete()
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(dinnerEndFocusRequester)
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
            onClick = { complete() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "완료")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
