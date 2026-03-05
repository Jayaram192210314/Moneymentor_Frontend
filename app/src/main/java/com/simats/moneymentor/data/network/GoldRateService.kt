package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.GoldRateResponse
import retrofit2.http.GET

interface GoldRateService {
    @GET("gold")
    suspend fun getGoldRates(): GoldRateResponse
}
