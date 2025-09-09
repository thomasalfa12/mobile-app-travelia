package com.travala.driver.ui.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.ui.screens.dashboard.ActiveTrip
import com.travala.driver.utils.TripManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OfferViewState(
    val isLoading: Boolean = false,
    val countdown: Int = 45,
    val offerResult: Result<ActiveTrip?>? = null // Success contains trip, Failure contains error
)

class OfferViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _viewState = MutableStateFlow(OfferViewState())
    val viewState = _viewState.asStateFlow()

    private var countdownJob: Job? = null

    fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 45 downTo 0) {
                _viewState.update { it.copy(countdown = i) }
                delay(1000)
            }
            // Jika waktu habis dan belum ada aksi, anggap ditolak
            if (_viewState.value.offerResult == null) {
                _viewState.update { it.copy(offerResult = Result.failure(Exception("Waktu habis"))) }
            }
        }
    }

    fun acceptOffer(bookingId: String) {
        countdownJob?.cancel()
        _viewState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val response = authRepository.acceptOffer(bookingId)
                if (response.isSuccessful && response.body() != null) {
                    val newTrip = response.body()!!
                    // Tulis ke TripManager agar Dashboard tahu
                    TripManager.startNewTrip(newTrip)
                    _viewState.update { it.copy(isLoading = false, offerResult = Result.success(newTrip)) }
                } else {
                    _viewState.update { it.copy(isLoading = false, offerResult = Result.failure(Exception("Gagal menerima tawaran: ${response.message()}"))) }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, offerResult = Result.failure(e)) }
            }
        }
    }

    fun rejectOffer(bookingId: String) {
        countdownJob?.cancel()
        _viewState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Kirim penolakan ke server
                authRepository.rejectOffer(bookingId)
            } catch (e: Exception) {
                // Abaikan error, yang penting tutup layar
            } finally {
                _viewState.update { it.copy(isLoading = false, offerResult = Result.failure(Exception("Tawaran ditolak"))) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}