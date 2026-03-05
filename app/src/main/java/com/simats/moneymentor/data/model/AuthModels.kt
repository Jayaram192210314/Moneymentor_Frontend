package com.simats.moneymentor.data.model

import com.google.gson.annotations.SerializedName

// --- Login Models ---
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserResponse? = null,
    @SerializedName("token") val token: String? = null
)

// --- Register Models ---
data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class RegisterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// --- Forgot Password Models ---
data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// --- Verify OTP Models ---
data class VerifyOtpRequest(
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// --- Reset Password Models ---
data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class ResetPasswordResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class GetProfileResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val user: UserResponse? = null
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("dob") val dob: String
)



// --- Common Models ---
data class UserResponse(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("fullName", alternate = ["full_name"]) val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone", alternate = ["mobile"]) val phone: String? = null,
    @SerializedName("dob") val dob: String? = null
)
