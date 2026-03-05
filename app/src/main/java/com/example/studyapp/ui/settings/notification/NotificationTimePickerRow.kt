package com.example.studyapp.ui.settings.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimePickerRow() {

    val hours = (4..23).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "10", "20", "30", "40", "50")

    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }

    var selectedHour by remember { mutableStateOf("08") }
    var selectedMinute by remember { mutableStateOf("00") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text("알람 시간 설정")

        Row(verticalAlignment = Alignment.CenterVertically) {

            // ⏰ Hour
            ExposedDropdownMenuBox(
                expanded = hourExpanded,
                onExpandedChange = { hourExpanded = !hourExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .width(90.dp)
                        .height(50.dp),
                    value = selectedHour,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(hourExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = hourExpanded,
                    onDismissRequest = { hourExpanded = false }
                ) {
                    hours.forEach { hour ->
                        DropdownMenuItem(
                            text = { Text(hour) },
                            onClick = {
                                selectedHour = hour
                                hourExpanded = false
                            }
                        )
                    }
                }
            }

            Text(" : ", modifier = Modifier.padding(horizontal = 12.dp))

            // ⏱ Minute
            ExposedDropdownMenuBox(
                expanded = minuteExpanded,
                onExpandedChange = { minuteExpanded = !minuteExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .width(90.dp)
                        .height(50.dp),
                    value = selectedMinute,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(minuteExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = minuteExpanded,
                    onDismissRequest = { minuteExpanded = false }
                ) {
                    minutes.forEach { min ->
                        DropdownMenuItem(
                            text = { Text(min) },
                            onClick = {
                                selectedMinute = min
                                minuteExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
