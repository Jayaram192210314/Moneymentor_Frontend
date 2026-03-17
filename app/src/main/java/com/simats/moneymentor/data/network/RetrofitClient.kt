package com.simats.moneymentor.data.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.90.27.238:5000/"

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.let { it.host } // Force property access via let to satisfy OkHttp in Kotlin
            cookieStore[host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val host = url.let { it.host }
            return cookieStore[host] ?: listOf()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val goldRateService: GoldRateService by lazy {
        retrofit.create(GoldRateService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val insightService: InsightService by lazy {
        retrofit.create(InsightService::class.java)
    }

    val goalService: GoalService by lazy {
        retrofit.create(GoalService::class.java)
    }

    val silverRateService: SilverRateService by lazy {
        retrofit.create(SilverRateService::class.java)
    }

    val learningService: LearningService by lazy {
        retrofit.create(LearningService::class.java)
    }


    val notificationService: NotificationService by lazy {
        retrofit.create(NotificationService::class.java)
    }    // Keep "instance" for backward compatibility if needed, but migrate to goldRateService
    val instance: GoldRateService get() = goldRateService
}

