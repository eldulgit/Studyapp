package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrioritySelector(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = "중요도")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..5).forEach { priority ->
                if (selectedPriority == priority) {
                    Button(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        onClick = { onPrioritySelected(priority) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(priority.toString())
                        }
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        onClick = { onPrioritySelected(priority) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(priority.toString())
                        }
                    }
                }
            }
        }
    }
}