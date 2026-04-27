package com.example.studyapp.ui.onboarding

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LifestyleInputScreen(
    onCompleteClick: (
        wakeTime: String,
        sleepTime: String,
        exercise: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var wakeTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var exercise by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                if (wakeTime.isBlank() || sleepTime.isBlank()) {
                    errorMessage = "기상 시간과 취침 시간 입력."
                    return@Button
                }

                onCompleteClick(
                    wakeTime,
                    sleepTime,
                    exercise
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "완료")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}