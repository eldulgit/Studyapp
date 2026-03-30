package com.example.studyapp.ui.settings.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.studyapp.util.ImageUtil.createImageUri
import com.example.studyapp.ui.settings.profile.UserViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun SettingHeader() {

    val context = LocalContext.current
    val userViewModel: UserViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        userViewModel.loadUserProfile()
    }

    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { profileImageUri = it.toString() }
            showPicker = false
        }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                cameraImageUri?.let {
                    profileImageUri = it.toString()
                }
            }
            showPicker = false
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        // 🟢 프로필 이미지
        Box(
            modifier = Modifier
                .size(110.dp)   // 살짝 줄여서 더 예쁘게
                .background(Color.LightGray, CircleShape)
                .clickable { showPicker = true },
            contentAlignment = Alignment.Center
        ) {

            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        // 📷 이미지 선택 팝업
        if (showPicker) {
            AlertDialog(
                onDismissRequest = { showPicker = false },
                title = { Text("프로필 이미지 선택") },
                text = {
                    Column {
                        Text(
                            "사진 촬영",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uri = createImageUri(context)
                                    cameraImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                                .padding(12.dp)
                        )

                        Text(
                            "갤러리에서 선택",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    galleryLauncher.launch("image/*")
                                }
                                .padding(12.dp)
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPicker = false }) {
                        Text("취소")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            // 이름은 항상 중앙 고정
            Text(
                text = userViewModel.userName,
                style = MaterialTheme.typography.titleLarge
            )

            // 아이콘은 이름 오른쪽에만 위치
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-120).dp)   // 이미지 중심 기준 위치 보정
                    .size(20.dp)
                    .clickable {
                        tempName = userViewModel.userName
                        showNameDialog = true
                    }
            )
        }


        // ✏ 이름 수정 팝업
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("이름 변경") },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userViewModel.onUserNameChanged(tempName)
                            userViewModel.saveUserName()
                            showNameDialog = false
                        }
                    ) {
                        Text("저장")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}