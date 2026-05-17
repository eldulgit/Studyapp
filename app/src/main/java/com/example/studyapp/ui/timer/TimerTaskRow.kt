package com.example.studyapp.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerTaskRow(
    subject: String,
    time: String,
    subjectColorArgb: Int,   // 추가
    containerWidth: Dp,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onEditClick: () -> Unit
) {
    val touchSize = 36.dp
    val playWidth = containerWidth * 0.10f
    val subjectNameWidth = containerWidth * 0.30f
    val colorWidth = containerWidth * 0.10f
    val timeWidth = containerWidth * 0.40f
    val editWidth = containerWidth * 0.10f

    Row(
        modifier = Modifier
            .width(containerWidth)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(playWidth),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(touchSize)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier.width(subjectNameWidth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subject,
                fontSize = 16.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier.width(colorWidth),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = Color(subjectColorArgb),
                        shape = CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier.width(timeWidth),
            contentAlignment = Alignment.Center
        ) {
            Text(time, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier.width(editWidth),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(touchSize)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
