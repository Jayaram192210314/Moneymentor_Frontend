package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.*
import retrofit2.Call
import retrofit2.http.*

interface NotificationService {
    @POST("/enable-daily")
    suspend fun enableDaily(@Body request: EnableDailyRequest): SimpleResponse

    @POST("/enable-monthly-flexible")
    suspend fun enableMonthly(@Body request: EnableMonthlyRequest): SimpleResponse

    @POST("/disable-notification")
    suspend fun disableNotification(@Body request: DisableNotificationRequest): SimpleResponse

    @GET("/get-notifications")
    suspend fun getNotifications(@Query("user_id") userId: Int): NotificationResponse

    @DELETE("/delete-notification/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): SimpleResponse
}
