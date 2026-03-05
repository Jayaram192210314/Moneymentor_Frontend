package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class DailyTipResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("daily_tip") val dailyTip: String? = null
)

data class DailyTermResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("daily_term") val dailyTerm: String? = null
)
