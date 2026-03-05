package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

data class ProgressUpdateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("article_no") val articleNo: Int
)

data class ProgressUpdateResponse(
    @SerializedName("status") val status: String
)

data class ProgressResponse(
    @SerializedName("completed") val completed: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("progress_percent") val progressPercent: Double,
    @SerializedName("completed_articles") val completedArticles: List<Int>? = null
)
