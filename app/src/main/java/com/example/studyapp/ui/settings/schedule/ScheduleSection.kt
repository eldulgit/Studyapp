package com.example.studyapp.ui.settings.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//목표화면
@Composable
fun ScheduleSection(
    title: String,
    items: List<FixedScheduleItem>,
    subtitleBuilder: (FixedScheduleItem) -> String,
    onEditClick: (FixedScheduleItem) -> Unit
) {
    var checkedItemIds by remember { mutableStateOf(setOf<Long>()) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

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
                items.forEach { item ->
                    SubjectSettingCard(
                        title = item.title,
                        subtitle = subtitleBuilder(item),
                        checked = item.id in checkedItemIds,
                        onCheckedChange = { checked ->
                            checkedItemIds =
                                if (checked) checkedItemIds + item.id
                                else checkedItemIds - item.id
                        },
                        onEditClick = {
                            onEditClick(item)
                        }
                    )
                }
            }
        }
    }
}