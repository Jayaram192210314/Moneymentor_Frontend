package com.simats.moneymentor.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OutOfQuotaPolicy

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Reschedule everything on boot or time change
        android.util.Log.d("MoneyMentorNotif", "Receiver triggered by action: ${intent.action}")

        if (intent.action == "android.intent.action.BOOT_COMPLETED" || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.intent.action.TIME_SET" ||
            intent.action == "android.intent.action.TIMEZONE_CHANGED") {
            NotificationScheduler.scheduleReminders(context)
            return
        }

        // Trigger immediate work to show the notification
        // No network constraint here; the worker manages its own retries
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "NotificationExecution",
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
