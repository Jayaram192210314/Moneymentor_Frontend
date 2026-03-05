package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class SilverRateResponse(
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("silver_today") val silverPrice: String? = null,
    @SerializedName("place") val place: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("today_change") val todayChange: String? = null,
    @SerializedName("table") val table: List<SilverRateHistory>? = null
)

data class SilverRateHistory(
    @SerializedName("date") val date: String? = null,
    @SerializedName("rate") val price: Double? = null,
    @SerializedName("change") val change: String? = null
)
