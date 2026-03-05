package com.simats.moneymentor.data

data class UserProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val dob: String,
    val id: Int = 5
)
