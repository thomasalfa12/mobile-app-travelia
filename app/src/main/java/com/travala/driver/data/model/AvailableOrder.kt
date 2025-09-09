package com.travala.driver.data.model

/**
 * Mewakili satu item orderan on-the-spot yang tersedia untuk diambil.
 */
data class AvailableOrder(
    val bookingId: Int,
    val route: String,
    val fare: Int,
    val passengerCount: Int,
    val pickupPoint: String
)
