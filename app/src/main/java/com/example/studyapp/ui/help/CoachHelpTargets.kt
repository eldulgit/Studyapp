package com.example.studyapp.ui.help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

object CoachHelpTargets {
    const val ScheduleSettingAdd = "schedule_setting_add"
    const val SubjectSettingMenu = "subject_setting_menu"
    const val SubjectSave = "subject_save"
    const val ScheduleGenerate = "schedule_generate"
    const val TimerCamera = "timer_camera"
    const val StatsFilter = "stats_filter"
    const val StatsTotalChart = "stats_total_chart"
    const val StatsFocusChart = "stats_focus_chart"
    const val HelpMenu = "help_menu"
}

@Stable
class CoachHelpTargetState {
    private val boundsByKey = mutableStateMapOf<String, Rect>()

    fun boundsFor(key: String?): Rect? {
        return key?.let(boundsByKey::get)
    }

    fun update(key: String, bounds: Rect) {
        boundsByKey[key] = bounds
    }

    fun remove(key: String) {
        boundsByKey.remove(key)
    }
}

val LocalCoachHelpTargetState = compositionLocalOf<CoachHelpTargetState?> { null }

@Composable
fun rememberCoachHelpTargetState(): CoachHelpTargetState {
    return remember { CoachHelpTargetState() }
}

fun Modifier.coachHelpTarget(key: String): Modifier = composed {
    val targetState = LocalCoachHelpTargetState.current

    DisposableEffect(targetState, key) {
        onDispose {
            targetState?.remove(key)
        }
    }

    if (targetState == null) {
        this
    } else {
        onGloballyPositioned { coordinates ->
            targetState.update(key, coordinates.boundsInRoot())
        }
    }
}
