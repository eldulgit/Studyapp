package com.example.studyapp.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem.Calendar,
        BottomNavItem.Todo,
        BottomNavItem.Timer,
        BottomNavItem.Setting
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = false, // 아직 상태 처리 안 함
                onClick = {
                    navController.navigate(item.route)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}
