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
import com.example.studyapp.ui.settings.schedule.GoalItem
import com.example.studyapp.util.AppTimeZone
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object GoalNotificationScheduler {
    const val CHANNEL_ID = "goal_reminder_alarm"

    private const val SHOW_REQUEST_CODE = 2000
    private const val REQUEST_CODE_BASE = 200_000
    private const val PREFS_NAME = "goal_reminder_alarms"
    private const val KEY_REQUEST_CODES = "request_codes"
    private val reminderDays = listOf(7, 3, 2, 1)

    fun scheduleGoalReminders(
        context: Context,
        goals: List<GoalItem>,
        hour: String,
        minute: String
    ) {
        createNotificationChannel(context)
        cancelGoalReminders(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCodes = mutableSetOf<String>()
        val now = System.currentTimeMillis()

        goals.forEach { goal ->
            val endDate = runCatching { LocalDate.parse(goal.endDate) }.getOrNull()
                ?: return@forEach

            reminderDays.forEach { daysBefore ->
                val triggerAtMillis = triggerAtMillis(
                    endDate = endDate,
                    daysBefore = daysBefore,
                    hour = hour,
                    minute = minute
                )

                if (triggerAtMillis <= now) return@forEach

                val requestCode = requestCode(goal.id, daysBefore)
                val pendingIntent = reminderPendingIntent(
                    context = context,
                    requestCode = requestCode,
                    goal = goal,
                    daysBefore = daysBefore
                )

                try {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(
                            triggerAtMillis,
                            appLaunchPendingIntent(context)
                        ),
                        pendingIntent
                    )
                    requestCodes.add(requestCode.toString())
                    Log.d(
                        "GoalReminder",
                        "목표 알림 예약 goal=${goal.title} daysBefore=$daysBefore triggerAtMillis=$triggerAtMillis"
                    )
                } catch (e: RuntimeException) {
                    Log.e("GoalReminder", "목표 알림 예약 실패", e)
                }
            }
        }

        saveRequestCodes(context, requestCodes)
    }

    fun cancelGoalReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCodes = loadRequestCodes(context)

        requestCodes.forEach { requestCodeText ->
            val requestCode = requestCodeText.toIntOrNull() ?: return@forEach
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    Intent(context, GoalReminderReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        saveRequestCodes(context, emptySet())
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "목표 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "목표 마감일이 가까워졌을 때 알려줍니다."
            enableVibration(true)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun reminderPendingIntent(
        context: Context,
        requestCode: Int,
        goal: GoalItem,
        daysBefore: Int
    ): PendingIntent {
        val intent = Intent(context, GoalReminderReceiver::class.java).apply {
            putExtra(GoalReminderReceiver.EXTRA_GOAL_TITLE, goal.title)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_END_DATE, goal.endDate)
            putExtra(GoalReminderReceiver.EXTRA_DAYS_BEFORE, daysBefore)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
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

    private fun triggerAtMillis(
        endDate: LocalDate,
        daysBefore: Int,
        hour: String,
        minute: String
    ): Long {
        val safeHour = hour.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val safeMinute = minute.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val zoneId = ZoneId.of(AppTimeZone.timeZone.id)

        return endDate
            .minusDays(daysBefore.toLong())
            .atTime(LocalTime.of(safeHour, safeMinute))
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun requestCode(goalId: String, daysBefore: Int): Int {
        return REQUEST_CODE_BASE + kotlin.math.abs("$goalId-$daysBefore".hashCode() % 100_000)
    }

    private fun loadRequestCodes(context: Context): Set<String> {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_REQUEST_CODES, emptySet())
            .orEmpty()
    }

    private fun saveRequestCodes(context: Context, requestCodes: Set<String>) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_REQUEST_CODES, requestCodes)
            .apply()
    }
}
