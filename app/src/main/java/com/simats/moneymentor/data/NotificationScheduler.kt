package com.simats.moneymentor.data

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleReminders(context: Context) {
        val prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val workManager = WorkManager.getInstance(context)

        // 1. General Polling (Less frequent now, for system updates)
        val generalRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(2, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork("NotificationPolling", ExistingWorkPolicy.KEEP, generalRequest)

        // 2. Daily Reminder (Exact Timing)
        if (prefs.getBoolean("daily_enabled", true)) {
            val timeStr = prefs.getString("daily_time", "09:00 AM") ?: "09:00 AM"
            scheduleSpecificWork(context, "DailyReminder", timeStr)
        } else {
            cancelSpecificWork(context, "DailyReminder")
        }

        // 3. Monthly Reminder (Exact Timing)
        if (prefs.getBoolean("monthly_enabled", true)) {
            val day = prefs.getString("monthly_day", "1")?.toIntOrNull() ?: 1
            val timeStr = prefs.getString("monthly_time", "10:00 AM") ?: "10:00 AM"
            scheduleSpecificWork(context, "MonthlyReminder", timeStr, day)
        } else {
            cancelSpecificWork(context, "MonthlyReminder")
        }
    }

    private fun cancelSpecificWork(context: Context, uniqueName: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, NotificationReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 
                uniqueName.hashCode(), 
                intent, 
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleSpecificWork(context: Context, uniqueName: String, timeStr: String, dayOfMonth: Int? = null) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, NotificationReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 
                uniqueName.hashCode(), 
                intent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val delay = calculateDelay(timeStr, dayOfMonth)
            val triggerTime = System.currentTimeMillis() + delay
            
            android.util.Log.d("MoneyMentorNotif", "Scheduling $uniqueName for $timeStr (Day: $dayOfMonth). Delay: ${delay/1000}s, Trigger: ${java.util.Date(triggerTime)}")

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to non-exact if permission is missing
                    alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun calculateDelay(timeStr: String, dayOfMonth: Int? = null): Long {
        try {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            val regex = Regex("(\\d+):(\\d+)\\s*(AM|PM)", RegexOption.IGNORE_CASE)
            val match = regex.find(timeStr.trim()) ?: return 15 * 60 * 1000L

            var hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].toInt()
            val amPm = match.groupValues[3]

            if (amPm.contains("PM", ignoreCase = true) && hour < 12) hour += 12
            if (amPm.contains("AM", ignoreCase = true) && hour == 12) hour = 0

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (dayOfMonth != null) {
                // Monthly logic
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.MONTH, 1)
                }
            } else {
                // Daily logic
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            android.util.Log.d("MoneyMentorNotif", "Next alarm set for: ${calendar.time} (Day: $dayOfMonth)")
            return calendar.timeInMillis - now
        } catch (e: Exception) {
            return 15 * 60 * 1000L
        }
    }
}
