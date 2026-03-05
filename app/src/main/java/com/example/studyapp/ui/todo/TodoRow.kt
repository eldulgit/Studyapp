package com.example.studyapp.ui.todo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TodoRow(
    todo: TodoItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = todo.isDone,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = todo.title,
            fontSize = 16.sp,
            textDecoration =
                if (todo.isDone)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
        )
    }
}
