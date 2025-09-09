package com.travala.driver.data.model

data class StatusUpdateRequest(
    val driverProfileId: Int,
    val status: String // "AKTIF" atau "NONAKTIF"
)