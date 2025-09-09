package com.travala.driver.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Sealed class untuk merepresentasikan state dari layar login,
 * apakah sedang di tahap input nomor HP atau verifikasi OTP.
 */
sealed class LoginStep {
    object PhoneEntry : LoginStep()
    object OtpVerification : LoginStep()
}

data class LoginUiState(
    val currentStep: LoginStep = LoginStep.PhoneEntry,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var currentWhatsappNumber: String = ""

    /**
     * Tahap 1: Meminta backend mengirimkan kode OTP ke nomor WhatsApp.
     */
    fun requestOtp(whatsappNumber: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // [PERBAIKAN] Panggil endpoint request-otp yang sebenarnya
                val response = authRepository.requestOtp(whatsappNumber)

                if (response.isSuccessful) {
                    currentWhatsappNumber = whatsappNumber // Simpan nomor untuk tahap verifikasi
                    _uiState.update {
                        it.copy(isLoading = false, currentStep = LoginStep.OtpVerification)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Gagal mengirim kode OTP. Pastikan nomor terdaftar.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan: ${e.message}")
                }
            }
        }
    }

    /**
     * Tahap 2: Mengirim nomor HP dan OTP ke backend untuk verifikasi.
     */
    fun verifyOtpAndLogin(otp: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // [PERBAIKAN] Panggil endpoint verify-otp yang sebenarnya
                val response = authRepository.verifyOtp(currentWhatsappNumber, otp)
                if (response.isSuccessful && response.body() != null) {
                    val driverData = response.body()!!

                    // Simpan token dan info driver
                    SessionManager.saveAuthToken(driverData.token)
                    SessionManager.saveDriverInfo(driverData.driverId, driverData.name)

                    // Kirim FCM Token secara proaktif setelah login
                    sendFcmTokenToServer()

                    // Update state UI untuk menandakan sukses
                    _uiState.update {
                        it.copy(isLoading = false, loginSuccess = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Kode OTP salah atau tidak valid.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan: ${e.message}")
                }
            }
        }
    }

    private suspend fun sendFcmTokenToServer() {
        try {
            // Minta token terbaru dari Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            val driverId = SessionManager.getDriverId()
            if (driverId != -1) {
                authRepository.registerFcmToken(driverId, token)
                Log.d("LoginViewModel", "FCM Token berhasil dikirim setelah login.")
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Gagal mendapatkan atau mengirim FCM token.", e)
        }
    }
}
