package com.simats.moneymentor.data

import com.simats.moneymentor.data.model.*
import com.simats.moneymentor.data.network.NotificationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationRepository(private val service: NotificationService) {

    suspend fun enableDaily(userId: Int, time: String): Result<String> {
        return try {
            val response = service.enableDaily(EnableDailyRequest(userId, time))
            Result.success(response.message ?: "Success")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enableMonthly(userId: Int, day: Int, time: String): Result<String> {
        return try {
            val response = service.enableMonthly(EnableMonthlyRequest(userId, day, time))
            Result.success(response.message ?: "Success")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disableNotification(userId: Int, type: String): Result<String> {
        return try {
            val response = service.disableNotification(DisableNotificationRequest(userId, type))
            Result.success(response.message ?: "Success")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(userId: Int): List<NotificationData>? {
        return try {
            val response = service.getNotifications(userId)
            response.notifications
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteNotification(id: Int): Boolean {
        return try {
            service.deleteNotification(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}
