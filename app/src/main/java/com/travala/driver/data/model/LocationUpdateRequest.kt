package com.travala.driver.data.model

data class LocationUpdateRequest(
    val driverProfileId: Int,
    val latitude: Double,
    val longitude: Double
)