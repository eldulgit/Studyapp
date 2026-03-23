package com.example.studyapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    val hasBackground = backgroundColor != Color.Transparent
    val isToday = date == LocalDate.now()

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        label = "border"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .border(
                width = borderWidth,
                color = if (isSelected)
                    MaterialTheme.colorScheme.secondary
                else
                    Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                enabled = date != null,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        date?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = it.dayOfMonth.toString(),
                    color = if (hasBackground)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
