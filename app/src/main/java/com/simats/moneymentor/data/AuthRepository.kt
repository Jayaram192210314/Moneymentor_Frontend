package com.simats.moneymentor.data

import com.simats.moneymentor.data.model.*
import com.simats.moneymentor.data.network.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val authService: AuthService) {

    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.login(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.register(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.forgotPassword(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.verifyOtp(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val json = com.google.gson.JsonParser().parse(errorBody).asJsonObject
                json.get("message")?.asString ?: errorBody ?: "Unknown error"
            } catch (ex: Exception) {
                errorBody ?: "HTTP error ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.resetPassword(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(userId: Int): Result<GetProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.getProfile(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userId: Int, request: UpdateProfileRequest): Result<com.simats.moneymentor.data.model.GenericResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authService.updateProfile(userId, request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
