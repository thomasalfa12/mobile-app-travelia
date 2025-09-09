package com.travala.driver.data.model

data class LoginResponse(
    val driverId: Int,
    val name: String,
    val token: String
)