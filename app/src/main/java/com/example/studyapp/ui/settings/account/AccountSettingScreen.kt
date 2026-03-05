package com.example.studyapp.ui.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AccountSettingScreen(navController: NavController){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "계정",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                onClick = { /* 나중에 로그인 연결 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Google로 로그인")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedButton(
                onClick = { /* 나중에 로그아웃 연결 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("로그아웃")
            }

        }
    }
}