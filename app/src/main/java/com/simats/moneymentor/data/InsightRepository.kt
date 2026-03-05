package com.simats.moneymentor.data

import com.simats.moneymentor.data.model.DailyTipResponse
import com.simats.moneymentor.data.model.DailyTermResponse
import com.simats.moneymentor.data.network.InsightService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsightRepository(private val insightService: InsightService) {

    suspend fun getDailyTip(): Result<DailyTipResponse> = withContext(Dispatchers.IO) {
        try {
            val response = insightService.getDailyTip()
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyTerm(): Result<DailyTermResponse> = withContext(Dispatchers.IO) {
        try {
            val response = insightService.getDailyTerm()
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
