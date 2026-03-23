package com.example.studyapp.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TimeEditDialog(
    show: Boolean,
    initialTime: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    if (!show) return

    val parsedHour = initialTime
        .substringBefore("H")
        .trim()
        .ifBlank { "0" }

    val parsedMinute = initialTime
        .substringAfter("H")
        .substringBefore("M")
        .trim()
        .ifBlank { "0" }

    var hour by remember(initialTime) { mutableStateOf(parsedHour) }
    var minute by remember(initialTime) { mutableStateOf(parsedMinute) }

    fun currentTotalMinutes(): Int {
        val h = hour.toIntOrNull() ?: 0
        val m = minute.toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun applyTotalMinutes(totalMinutes: Int) {
        val safeTotal = totalMinutes.coerceAtLeast(0)
        hour = (safeTotal / 60).toString()
        minute = (safeTotal % 60).toString()
    }

    fun addMinutes(extraMinutes: Int) {
        applyTotalMinutes(currentTotalMinutes() + extraMinutes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "시간 수정",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeInputField(
                        label = "시간",
                        value = hour,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                hour = input
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TimeInputField(
                        label = "분",
                        value = minute,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                minute = input
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "빠른 추가",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickTimeChip(
                            text = "+10분",
                            modifier = Modifier.weight(1f)
                        ) { addMinutes(10) }

                        QuickTimeChip(
                            text = "+30분",
                            modifier = Modifier.weight(1f)
                        ) { addMinutes(30) }

                        QuickTimeChip(
                            text = "+1시간",
                            modifier = Modifier.weight(1f)
                        ) { addMinutes(60) }
                    }
                }

                Text(
                    text = "현재 설정 · ${hour.ifBlank { "0" }}시간 ${minute.ifBlank { "0" }}분",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val total = currentTotalMinutes()
                    onSave(
                        (total / 60).toString(),
                        (total % 60).toString()
                    )
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun TimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }
}

@Composable
private fun QuickTimeChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}