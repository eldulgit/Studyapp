package com.example.studyapp.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CoachHelpStep(
    val route: String,
    val title: String,
    val description: String,
    val placement: CoachHelpPlacement
)

enum class CoachHelpPlacement {
    Top,
    Center,
    Bottom
}

@Composable
fun CoachHelpOverlay(
    step: CoachHelpStep,
    currentStepIndex: Int,
    totalStepCount: Int,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f))
            .clickable(onClick = onNext)
            .padding(horizontal = 18.dp)
            .navigationBarsPadding(),
        contentAlignment = when (step.placement) {
            CoachHelpPlacement.Top -> Alignment.TopCenter
            CoachHelpPlacement.Center -> Alignment.Center
            CoachHelpPlacement.Bottom -> Alignment.BottomCenter
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (step.placement == CoachHelpPlacement.Top) 28.dp else 0.dp,
                    bottom = if (step.placement == CoachHelpPlacement.Bottom) 22.dp else 0.dp
                ),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${currentStepIndex + 1}/$totalStepCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (currentStepIndex == totalStepCount - 1) {
                        "화면을 터치하면 도움말이 끝나요"
                    } else {
                        "화면을 터치하면 다음 설명으로 넘어가요"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
