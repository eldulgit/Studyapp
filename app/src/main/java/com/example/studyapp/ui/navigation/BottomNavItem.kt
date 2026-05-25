package com.example.studyapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer

import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object ScheduleSetting : BottomNavItem(
        route = "setting_schedule",
        label = "Setup",
        icon = Icons.Default.GridView
    )

    object Calendar : BottomNavItem(
        route = "calendar",
        label = "Schedule",
        icon = Icons.Default.Schedule
    )

    object Timer : BottomNavItem(
        route = "timer",
        label = "Timer",
        icon = Icons.Default.Timer
    )

    object Stats : BottomNavItem(
        route = "stats",
        label = "Stats",
        icon = Icons.Filled.BarChart
    )



    object Setting : BottomNavItem(
        route = "setting",
        label = "Settings",
        icon = Icons.Default.Settings
    )
}
