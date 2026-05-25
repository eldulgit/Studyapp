package com.example.studyapp.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.studyapp.MainActivity
import com.example.studyapp.util.AppTimeZone
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object StudyNotificationScheduler {
    const val CHANNEL_ID = "study_reminder_alarm"
    const val NOTIFICATION_ID = 1001

    private const val REQUEST_CODE = 1001
    private const val SHOW_REQUEST_CODE = 1002

    fun scheduleDailyStudyReminder(
        context: Context,
        hour: String,
        minute: String
    ) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context)

        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = nextTriggerAtMillis(hour, minute)

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    triggerAtMillis,
                    appLaunchPendingIntent(context)
                ),
                pendingIntent
            )
            Log.d(
                "StudyReminder",
                "정확 알림 예약 time=$hour:$minute trigger=${formatTrigger(triggerAtMillis)}"
            )
        } catch (e: RuntimeException) {
            Log.e("StudyReminder", "정확 알림 예약 실패", e)
        }
    }

    fun cancelDailyStudyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(reminderPendingIntent(context))
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "공부 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "설정한 시간에 공부를 시작하도록 알려줍니다."
            enableVibration(true)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, StudyReminderReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun appLaunchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            context,
            SHOW_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerAtMillis(hour: String, minute: String): Long {
        val safeHour = hour.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val safeMinute = minute.toIntOrNull()?.coerceIn(0, 59) ?: 0

        return Calendar.getInstance(AppTimeZone.timeZone).apply {
            set(Calendar.HOUR_OF_DAY, safeHour)
            set(Calendar.MINUTE, safeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private fun formatTrigger(triggerAtMillis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
            timeZone = AppTimeZone.timeZone
        }
        return formatter.format(triggerAtMillis)
    }
}
