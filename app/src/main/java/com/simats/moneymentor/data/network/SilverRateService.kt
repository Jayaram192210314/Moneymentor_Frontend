package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.SilverRateResponse
import retrofit2.http.GET

interface SilverRateService {
    @GET("silver")
    suspend fun getSilverRates(): SilverRateResponse
}
