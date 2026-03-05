package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.ProgressResponse
import com.simats.moneymentor.data.model.ProgressUpdateRequest
import com.simats.moneymentor.data.model.ProgressUpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LearningService {
    @POST("/progress/update")
    suspend fun updateProgress(@Body request: ProgressUpdateRequest): ProgressUpdateResponse

    @GET("/progress/{user_id}")
    suspend fun getProgress(@Path("user_id") userId: Int): ProgressResponse
}
