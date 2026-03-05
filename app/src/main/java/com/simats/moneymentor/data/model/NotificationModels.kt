package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("notifications") val notifications: List<NotificationData> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class NotificationData(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("time") val time: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_unread") val isUnread: Boolean
)

data class EnableDailyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("time") val time: String
)

data class EnableMonthlyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("time") val time: String
)

data class DisableNotificationRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type") val type: String
)

data class SimpleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
