package com.travala.driver.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.utils.SessionManager
import com.travala.driver.utils.TripManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// [PERUBAHAN] Data dummy diperbarui dengan nomor WhatsApp yang Anda berikan
private val dummyTripForPreview = ActiveTrip(
    tripId = 1,
    finalDestination = "Indralaya",
    remainingCapacity = 4, // Disesuaikan dengan 3 penumpang
    totalCapacity = 7,
    tasks = listOf(
        PickupTask(bookingId = 101, passengerName = "Budi Santoso", location = "Gerbang Utama Unsri Bukit", passengerWaNumber = "+6285156566928", isCompleted = true),
        PickupTask(bookingId = 102, passengerName = "Citra Lestari", location = "Kambang Iwak, Palembang", passengerWaNumber = "+6285888801682", isCompleted = false),
        PickupTask(bookingId = 103, passengerName = "Dewi Anggraini", location = "Simpang Polda, Palembang", passengerWaNumber = "+6281266757555", isCompleted = false)
    )
)

data class ViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SnackbarOffer(
    val bookingId: String,
    val text: String
)

class DashboardViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // !! PENTING: Kembalikan ke DashboardState.Offline setelah UI selesai !!
    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Offline)
    val dashboardState = _dashboardState.asStateFlow()

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    private val _serviceEvent = MutableSharedFlow<ServiceAction>()
    val serviceEvent = _serviceEvent.asSharedFlow()

    private val _newOfferSnackbar = MutableStateFlow<SnackbarOffer?>(null)
    val newOfferSnackbar = _newOfferSnackbar.asStateFlow()

    init {
        // Pantau terus TripManager untuk perjalanan baru
        viewModelScope.launch {
            TripManager.currentTrip.collectLatest { trip ->
                if (trip != null) {
                    _dashboardState.value = DashboardState.InTrip(trip)
                }
            }
        }
    }

    fun showInTripOffer(bookingId: String, text: String) {
        _newOfferSnackbar.value = SnackbarOffer(bookingId, text)
    }

    fun dismissSnackbarOffer() {
        _newOfferSnackbar.value = null
    }

    fun setOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, errorMessage = null) }
            val newStatus = if (isOnline) "AKTIF" else "NONAKTIF"
            val driverId = SessionManager.getDriverId()
            if (driverId == -1) {
                _viewState.update { it.copy(isLoading = false, errorMessage = "Driver ID tidak ditemukan.") }
                return@launch
            }

            try {
                authRepository.updateStatus(driverId, newStatus)
                _dashboardState.value = if (isOnline) DashboardState.Searching else DashboardState.Offline
                if (isOnline) _serviceEvent.emit(ServiceAction.START) else _serviceEvent.emit(ServiceAction.STOP)
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Gagal update status: ${e.message}") }
            } finally {
                _viewState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun completePickupTask(bookingId: Int) {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            if (currentState is DashboardState.InTrip) {
                _viewState.update { it.copy(isLoading = true) }
                try {
                    val response = authRepository.completePickup(bookingId)
                    if (response.isSuccessful) {
                        // Update state lokal untuk mencoret tugas yang selesai
                        val updatedTasks = currentState.trip.tasks.map {
                            if (it.bookingId == bookingId) it.copy(isCompleted = true) else it
                        }
                        // Hitung ulang sisa kapasitas jika perlu
                        _dashboardState.value = currentState.copy(trip = currentState.trip.copy(tasks = updatedTasks))
                    } else {
                        _viewState.update { it.copy(errorMessage = "Gagal update status jemput.") }
                    }
                } catch (e: Exception) {
                    _viewState.update { it.copy(errorMessage = e.message) }
                } finally {
                    _viewState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun finishTrip() {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            if (currentState is DashboardState.InTrip) {
                _viewState.update { it.copy(isLoading = true) }
                try {
                    authRepository.completeTrip(currentState.trip.tripId)
                    TripManager.endCurrentTrip()
                    _dashboardState.value = DashboardState.Searching
                } catch (e: Exception) {
                    _viewState.update { it.copy(errorMessage = e.message) }
                } finally {
                    _viewState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // Fungsi ini dipanggil dari dialog konfirmasi saat supir menekan tombol back
    fun forceFinishTrip() {
        // TODO: Panggil API untuk membatalkan trip di server jika diperlukan
        TripManager.endCurrentTrip()
        _dashboardState.value = DashboardState.Searching
    }

    enum class ServiceAction { START, STOP }
}

