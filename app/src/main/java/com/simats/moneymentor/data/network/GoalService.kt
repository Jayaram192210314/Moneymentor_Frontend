package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.GoalResponse
import com.simats.moneymentor.data.model.SingleGoalResponse
import com.simats.moneymentor.data.model.GenericResponse
import com.simats.moneymentor.data.model.AddGoalRequest
import com.simats.moneymentor.data.model.UpdateExtraRequest
import com.simats.moneymentor.data.model.UpdateExtraResponse
import com.simats.moneymentor.data.model.WithdrawRequest
import com.simats.moneymentor.data.model.WithdrawResponse
import com.simats.moneymentor.data.model.UpdateGoalRequest
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface GoalService {
    @GET("get-goals")
    suspend fun getGoals(
        @Query("user_id") userId: Int
    ): GoalResponse

    @GET("get-goal/{goal_id}")
    suspend fun getGoalById(
        @Path("goal_id") goalId: Int
    ): SingleGoalResponse

    @POST("add-goal")
    suspend fun addGoal(
        @Body request: AddGoalRequest
    ): GenericResponse

    @PUT("update-extra/{goal_id}")
    suspend fun updateExtraAmount(
        @Path("goal_id") goalId: Int,
        @Body request: UpdateExtraRequest
    ): UpdateExtraResponse

    @PUT("withdraw/{goal_id}")
    suspend fun withdrawAmount(
        @Path("goal_id") goalId: Int,
        @Body request: WithdrawRequest
    ): WithdrawResponse

    @PUT("update-goal/{goal_id}")
    suspend fun updateGoalDetails(
        @Path("goal_id") goalId: Int,
        @Body request: UpdateGoalRequest
    ): GenericResponse

    @retrofit2.http.DELETE("delete-goal/{goal_id}")
    suspend fun deleteGoal(
        @Path("goal_id") goalId: Int
    ): GenericResponse
}
