package com.travala.driver.utils

import com.travala.driver.ui.screens.dashboard.ActiveTrip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object untuk mengelola state perjalanan aktif secara global.
 * Bertindak sebagai jembatan data antara OfferViewModel dan DashboardViewModel.
 */
object TripManager {

    private val _currentTrip = MutableStateFlow<ActiveTrip?>(null)
    val currentTrip = _currentTrip.asStateFlow()

    fun startNewTrip(trip: ActiveTrip) {
        _currentTrip.value = trip
    }

    fun endCurrentTrip() {
        _currentTrip.value = null
    }
}
