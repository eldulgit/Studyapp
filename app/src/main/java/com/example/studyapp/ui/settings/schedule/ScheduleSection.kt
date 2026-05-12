package com.example.studyapp.ui.settings.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 목표화면

val goalColors = listOf(
    Color(0xFFBDE0FE),
    Color(0xFFD0E6FF),
    Color(0xFFBFCBFF),
    Color(0xFFD9C2F0),
    Color(0xFFEADCF8)
)
@Composable
fun ScheduleSection(
    title: String,
    items: List<FixedScheduleItem>,
    subtitleBuilder: (FixedScheduleItem) -> String,
    onEditClick: (FixedScheduleItem) -> Unit,
    onCheckedChange: (FixedScheduleItem, Boolean) -> Unit = { _, _ -> },
    guideText: String? = null
) {


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (guideText != null) {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = guideText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            Text(
                text = "등록된 항목이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val goalColor = goalColors[index % goalColors.size]

                    SubjectSettingCard(
                        title = item.title,
                        subtitle = subtitleBuilder(item),
                        checked = item.increasePriorityOverTime,
                        onCheckedChange = { checked ->
                            onCheckedChange(item, checked)
                        },
                        onEditClick = {
                            onEditClick(item)
                        },
                        containerColor = goalColor
                    )
                }
            }
        }
    }
}