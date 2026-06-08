package com.example.studyapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.studyapp.R

class GoalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        createChannel(context)
        showNotificationIfAllowed(context, intent)
    }

    private fun showNotificationIfAllowed(context: Context, intent: Intent?) {
        val hasNotificationPermission = !(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
        )

        if (!hasNotificationPermission) return

        val goalTitle = intent?.getStringExtra(EXTRA_GOAL_TITLE).orEmpty()
        val endDate = intent?.getStringExtra(EXTRA_GOAL_END_DATE).orEmpty()
        val daysBefore = intent?.getIntExtra(EXTRA_DAYS_BEFORE, 0) ?: 0
        val title = if (daysBefore > 0) {
            "목표 마감 ${daysBefore}일 전이에요"
        } else {
            "목표 마감일이에요"
        }
        val content = if (goalTitle.isNotBlank() && endDate.isNotBlank()) {
            "\"$goalTitle\" 목표가 $endDate 에 끝나요."
        } else {
            "목표 마감일이 가까워졌어요."
        }

        val notification = NotificationCompat.Builder(context, GoalNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            notificationId(goalTitle, endDate, daysBefore),
            notification
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            GoalNotificationScheduler.CHANNEL_ID,
            "목표 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun notificationId(goalTitle: String, endDate: String, daysBefore: Int): Int {
        return 300_000 + kotlin.math.abs("$goalTitle-$endDate-$daysBefore".hashCode() % 100_000)
    }

    companion object {
        const val EXTRA_GOAL_TITLE = "extra_goal_title"
        const val EXTRA_GOAL_END_DATE = "extra_goal_end_date"
        const val EXTRA_DAYS_BEFORE = "extra_days_before"
    }
}
