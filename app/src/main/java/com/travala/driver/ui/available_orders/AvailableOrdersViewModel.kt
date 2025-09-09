package com.travala.driver.ui.available_orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travala.driver.data.model.AvailableOrder
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.utils.TripManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AvailableOrdersViewState(
    val isLoading: Boolean = true,
    val orders: List<AvailableOrder> = emptyList(),
    val error: String? = null,
    val claimSuccess: Boolean = false
)

class AvailableOrdersViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _viewState = MutableStateFlow(AvailableOrdersViewState())
    val viewState = _viewState.asStateFlow()

    init {
        fetchAvailableOrders()
    }

    fun fetchAvailableOrders() {
        _viewState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = authRepository.getAvailableBookings()
                if (response.isSuccessful && response.body() != null) {
                    _viewState.update { it.copy(isLoading = false, orders = response.body()!!) }
                } else {
                    _viewState.update { it.copy(isLoading = false, error = "Gagal memuat orderan.") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun acceptAvailableOrder(bookingId: Int) {
        _viewState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Gunakan endpoint 'acceptOffer' yang sudah ada
                val response = authRepository.acceptOffer(bookingId.toString())
                if (response.isSuccessful && response.body() != null) {
                    TripManager.startNewTrip(response.body()!!)
                    _viewState.update { it.copy(isLoading = false, claimSuccess = true) }
                } else {
                    _viewState.update { it.copy(isLoading = false, error = "Gagal mengambil orderan.") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
