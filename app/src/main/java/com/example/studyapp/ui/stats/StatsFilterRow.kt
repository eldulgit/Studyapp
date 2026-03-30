package com.example.studyapp.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsFilterRow(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit
) {
    val filters = listOf(
        StatsPeriod.DAILY to "Daily",
        StatsPeriod.WEEKLY to "Weekly",
        StatsPeriod.MONTHLY to "Monthly"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "기간별 공부시간",
            modifier = Modifier.padding(bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filters.forEach { (period, label) ->
                val isSelected = selected == period

                OutlinedButton(
                    onClick = { onSelect(period) },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                    enabled = !isSelected
                ) {
                    Text(label)
                }
            }
        }
    }
}