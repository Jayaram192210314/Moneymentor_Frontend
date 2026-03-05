package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.DailyTipResponse
import com.simats.moneymentor.data.model.DailyTermResponse
import retrofit2.http.GET

interface InsightService {
    @GET("daily-tip")
    suspend fun getDailyTip(): DailyTipResponse

    @GET("daily-term")
    suspend fun getDailyTerm(): DailyTermResponse
}
