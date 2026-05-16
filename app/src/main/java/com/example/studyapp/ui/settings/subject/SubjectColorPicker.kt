package com.example.studyapp.ui.settings.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun SubjectColorPicker(
    colors: List<Color>,
    selectedColorArgb: Int,
    disabledColorArgbList: List<Int> = emptyList(),
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { color ->
            val colorArgb = color.toArgb()
            val isSelected = selectedColorArgb == colorArgb
            val isDisabled = colorArgb in disabledColorArgbList

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isDisabled) Color.LightGray else color,
                        shape = CircleShape
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = when {
                            isSelected -> Color.White
                            isDisabled -> Color.Gray
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    )
                    .alpha(if (isDisabled) 0.55f else 1f)
                    .clickable(enabled = !isDisabled) {
                        onColorSelected(color)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isDisabled) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "이미 사용 중인 색상",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
