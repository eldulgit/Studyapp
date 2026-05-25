package com.example.studyapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.studyapp.R
import com.example.studyapp.ui.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        Log.d("StudyReminder", "알림 수신 receivedAt=${System.currentTimeMillis()}")
        createChannel(context)
        showNotificationIfAllowed(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduleNextReminderIfEnabled(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotificationIfAllowed(context: Context) {
        val hasNotificationPermission = !(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
        )

        if (!hasNotificationPermission) return

        val notification = NotificationCompat.Builder(context, StudyNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("공부할 시간이에요")
            .setContentText("오늘 공부도 차근차근 시작해볼까요?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            StudyNotificationScheduler.NOTIFICATION_ID,
            notification
        )
    }

    private suspend fun scheduleNextReminderIfEnabled(context: Context) {
        val repo = SettingsRepository(context)

        if (!repo.notificationEnabledFlow.first()) return

        StudyNotificationScheduler.scheduleDailyStudyReminder(
            context = context,
            hour = repo.notificationHourFlow.first(),
            minute = repo.notificationMinuteFlow.first()
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            StudyNotificationScheduler.CHANNEL_ID,
            "공부 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
