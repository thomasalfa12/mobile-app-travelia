package com.travala.driver.ui.screens.dashboard

// Model data untuk tugas/penjemputan, sekarang dengan bookingId
data class PickupTask(
    val bookingId: Int, // <-- DIPERBAIKI
    val passengerName: String,
    val location: String,
    val passengerWaNumber: String,
    val isCompleted: Boolean = false
)

// Model data untuk perjalanan aktif, sekarang dengan tripId
data class ActiveTrip(
    val tripId: Int, // <-- DIPERBAIKI
    val finalDestination: String,
    val remainingCapacity: Int,
    val totalCapacity: Int,
    val tasks: List<PickupTask>
)

// Sealed class untuk merepresentasikan semua kemungkinan state di halaman Dashboard
sealed class DashboardState {
    object Offline : DashboardState()
    object Searching : DashboardState()
    data class InTrip(val trip: ActiveTrip) : DashboardState()
}
