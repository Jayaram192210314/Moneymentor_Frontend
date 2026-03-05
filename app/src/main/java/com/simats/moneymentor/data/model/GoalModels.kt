package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class GoalResponse(
    val status: String,
    val goals: List<GoalItem>
)

data class GoalItem(
    val id: Int,
    @SerializedName("goal_name") val goalName: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("already_saved_amount") val alreadySavedAmount: Double,
    val deadline: String,
    @SerializedName("user_id") val userId: Int
)

data class SingleGoalResponse(
    val status: String,
    val goal: GoalItem
)

data class AddGoalRequest(
    @SerializedName("goal_name") val goalName: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("already_saved_amount") val alreadySavedAmount: Double,
    val deadline: String,
    @SerializedName("user_id") val userId: Int
)

data class UpdateExtraRequest(
    @SerializedName("extra_amount") val extraAmount: Double
)

data class UpdateExtraResponse(
    val status: String,
    val message: String,
    @SerializedName("updated_saved_amount") val updatedSavedAmount: Double? = null
)

data class WithdrawRequest(
    @SerializedName("withdraw_amount") val withdrawAmount: Double
)

data class WithdrawResponse(
    val status: String,
    val message: String,
    @SerializedName("updated_saved_amount") val updatedSavedAmount: Double? = null
)

data class UpdateGoalRequest(
    @SerializedName("goal_name") val goalName: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("already_saved_amount") val alreadySavedAmount: Double,
    val deadline: String
)

data class GenericResponse(
    val status: String,
    val message: String
)
