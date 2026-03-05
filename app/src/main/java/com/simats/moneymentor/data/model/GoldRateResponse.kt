package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class GoldRateResponse(
    @SerializedName("currency") val currency: String,
    @SerializedName("gold_price_24k_per_gram") val goldPrice: String,
    @SerializedName("place") val place: String,
    @SerializedName("silver") val silverPrice: String,
    @SerializedName("status") val status: String,
    @SerializedName("time") val time: String,
    @SerializedName("change_24k_today") val change24kToday: String? = null,
    @SerializedName("change_22k_today") val change22kToday: String? = null,
    @SerializedName("table") val table: List<GoldRateHistory>? = null
)

data class GoldRateHistory(
    @SerializedName("date") val date: String,
    @SerializedName("22k") val price22k: String,
    @SerializedName("24k") val price24k: String,
    @SerializedName("22k_change") val change22k: String?,
    @SerializedName("24k_change") val change24k: String?
)

