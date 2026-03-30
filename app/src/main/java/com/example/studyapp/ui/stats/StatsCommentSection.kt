package com.example.studyapp.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.studyapp.R

@Composable
fun StatsCommentSection(comment: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Image(
            painter = painterResource(id = R.drawable.blueberry_coach),
            contentDescription = "AI 코치 이미지",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text("AI 코치", color = Color.Gray)
            Text(comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}