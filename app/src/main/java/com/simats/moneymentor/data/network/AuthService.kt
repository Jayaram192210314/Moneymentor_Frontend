package com.simats.moneymentor.data.network

import com.simats.moneymentor.data.model.LoginRequest
import com.simats.moneymentor.data.model.LoginResponse
import com.simats.moneymentor.data.model.RegisterRequest
import com.simats.moneymentor.data.model.RegisterResponse
import com.simats.moneymentor.data.model.ForgotPasswordRequest
import com.simats.moneymentor.data.model.ForgotPasswordResponse
import com.simats.moneymentor.data.model.VerifyOtpRequest
import com.simats.moneymentor.data.model.VerifyOtpResponse
import com.simats.moneymentor.data.model.ResetPasswordRequest
import com.simats.moneymentor.data.model.ResetPasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    @retrofit2.http.GET("get-profile/{user_id}")
    suspend fun getProfile(@retrofit2.http.Path("user_id") userId: Int): com.simats.moneymentor.data.model.GetProfileResponse

    @retrofit2.http.PUT("update-profile/{user_id}")
    suspend fun updateProfile(
        @retrofit2.http.Path("user_id") userId: Int,
        @retrofit2.http.Body request: com.simats.moneymentor.data.model.UpdateProfileRequest
    ): com.simats.moneymentor.data.model.GenericResponse
}
