package com.example.studyapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Calendar : BottomNavItem(
        route = "calendar",
        label = "Calendar",
        icon = Icons.Default.CalendarToday
    )

    object Todo : BottomNavItem(
        route = "todo",
        label = "Todo",
        icon = Icons.Default.Checklist
    )

    object Timer : BottomNavItem(
        route = "timer",
        label = "Timer",
        icon = Icons.Default.Timer
    )

    object Setting : BottomNavItem(
        route = "setting",
        label = "Setting",
        icon = Icons.Default.Settings
    )
}
