package com.example.studyapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studyapp.data.repository.AuthRepository
import com.example.studyapp.data.repository.GoalRepository
import com.example.studyapp.ui.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudyReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val repo = SettingsRepository(context)
            val enabled = repo.notificationEnabledFlow.first()

            if (enabled) {
                StudyNotificationScheduler.scheduleDailyStudyReminder(
                    context = context,
                    hour = repo.notificationHourFlow.first(),
                    minute = repo.notificationMinuteFlow.first()
                )
            }

            if (repo.goalAlertEnabledFlow.first()) {
                val uid = AuthRepository().getCurrentUid() ?: return@launch
                val goals = GoalRepository().getGoals(uid)
                GoalNotificationScheduler.scheduleGoalReminders(
                    context = context,
                    goals = goals,
                    hour = repo.notificationHourFlow.first(),
                    minute = repo.notificationMinuteFlow.first()
                )
            } else {
                GoalNotificationScheduler.cancelGoalReminders(context)
            }
        }
    }
}
