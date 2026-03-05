package com.simats.moneymentor.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.moneymentor.data.network.RetrofitClient
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.simats.moneymentor.MainActivity
import com.simats.moneymentor.R
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.util.Log

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val TAG = "MoneyMentorNotif"

    override suspend fun doWork(): Result {
        val userId = applicationContext.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE).getInt("user_id", 0)
        Log.d(TAG, "Worker started for user: $userId")
        
        if (userId == 0) {
            Log.d(TAG, "No user ID found, stopping.")
            return Result.success() 
        }
        
        var lastError: Exception? = null
        // Internal immediate retry loop for transient network glitches
        for (attempt in 1..3) {
            try {
                Log.d(TAG, "Fetching notifications attempt $attempt...")
                val response = RetrofitClient.notificationService.getNotifications(userId)
                val notifications = response.notifications ?: emptyList()
                Log.d(TAG, "Received ${notifications.size} notifications total.")
                
                val unread = notifications.filter { it.isUnread }.sortedByDescending { it.id }
                
                if (unread.isNotEmpty()) {
                    val prefs = applicationContext.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
                    val lastNotifiedId = prefs.getInt("last_notified_id", -1)
                    
                    val newNotifications = unread.filter { it.id > lastNotifiedId }.take(3)
                    Log.d(TAG, "New notifications to show: ${newNotifications.size}")
                    
                    for (notif in newNotifications.reversed()) {
                        showNotification(notif.id, notif.title, notif.message)
                        if (newNotifications.size > 1) {
                            kotlinx.coroutines.delay(1500) // Stagger alerts by 1.5s to avoid "disturbance"
                        }
                    }
                    
                    if (newNotifications.isNotEmpty()) {
                        prefs.edit().putInt("last_notified_id", newNotifications.first().id).apply()
                    }
                }
                
                // If we reached here, success!
                Log.d(TAG, "Worker completed successfully.")
                NotificationScheduler.scheduleReminders(applicationContext) 
                return Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Attempt $attempt failed: ${e.message}")
                lastError = e
                if (attempt < 3) kotlinx.coroutines.delay(5000) // Wait 5s before next attempt
            }
        }
        
        // If all internal retries fail, let WorkManager handle the scheduled retry
        Log.w(TAG, "All internal retries failed. Scheduling WorkManager retry.")
        return Result.retry()
    }

    private fun showNotification(id: Int, title: String, message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, id, intent, PendingIntent.FLAG_IMMUTABLE)

        val groupKey = "com.simats.moneymentor.NOTIFICATIONS"
        val builder = NotificationCompat.Builder(applicationContext, "money_mentor_notifications")
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(groupKey)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Show actual notification
        notificationManager.notify(id, builder.build())

        // Show/Update summary notification for grouping
        val summaryBuilder = NotificationCompat.Builder(applicationContext, "money_mentor_notifications")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Money Mentor")
            .setContentText("You have new updates")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)

        notificationManager.notify(0, summaryBuilder.build())
    }
}
