package com.travala.driver.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travala.driver.data.model.Schedule
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.utils.TripManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleViewState(
    val isLoading: Boolean = true,
    val schedules: List<Schedule> = emptyList(),
    val claimResult: ClaimResult? = null,
    val errorMessage: String? = null
)

sealed class ClaimResult {
    object Success : ClaimResult()
    data class Error(val message: String) : ClaimResult()
}

class ScheduleViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _viewState = MutableStateFlow(ScheduleViewState())
    val viewState = _viewState.asStateFlow()

    init {
        fetchSchedules()
    }

    fun fetchSchedules() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = authRepository.getSchedules()
                if (response.isSuccessful && response.body() != null) {
                    _viewState.update { it.copy(isLoading = false, schedules = response.body()!!) }
                } else {
                    _viewState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat jadwal.") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun claimSchedule(scheduleId: Int) {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = authRepository.claimSchedule(scheduleId)
                if (response.isSuccessful && response.body() != null) {
                    TripManager.startNewTrip(response.body()!!)
                    _viewState.update { it.copy(isLoading = false, claimResult = ClaimResult.Success) }
                } else {
                    _viewState.update { it.copy(isLoading = false, claimResult = ClaimResult.Error("Jadwal sudah tidak tersedia.")) }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, claimResult = ClaimResult.Error(e.message ?: "Terjadi kesalahan")) }
            }
        }
    }
}
    
