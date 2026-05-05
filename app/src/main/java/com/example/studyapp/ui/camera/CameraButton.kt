package com.example.studyapp.ui.camera

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

// 카메라 모양의 버튼 UI 구현
@Composable
fun CameraButton(navController: NavController) {
    IconButton(onClick = { navController.navigate("camera") }) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "Camera"
        )
    }
}
