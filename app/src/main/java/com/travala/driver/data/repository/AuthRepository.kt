package com.travala.driver.data.repository

import com.travala.driver.data.api.RetrofitClient
import com.travala.driver.data.model.*
import com.travala.driver.ui.screens.dashboard.ActiveTrip
import retrofit2.Response

class AuthRepository {

    // --- Login & OTP ---
    suspend fun requestOtp(whatsapp: String): Response<Unit> {
        return RetrofitClient.instance.requestOtp(OtpRequest(whatsapp))
    }

    suspend fun verifyOtp(whatsapp: String, otp: String): Response<LoginResponse> {
        return RetrofitClient.instance.verifyOtp(OtpVerifyRequest(whatsapp, otp))
    }

    // --- Driver Management ---
    suspend fun updateStatus(driverProfileId: Int, status: String): Response<Unit> {
        val request = StatusUpdateRequest(driverProfileId, status)
        return RetrofitClient.instance.updateStatus(request)
    }

    suspend fun updateLocation(req: LocationUpdateRequest): Response<Unit> {
        return RetrofitClient.instance.updateLocation(req)
    }

    suspend fun registerFcmToken(driverId: Int, token: String): Response<Unit> {
        val request = FcmTokenRequest(driverProfileId = driverId, fcmToken = token)
        return RetrofitClient.instance.registerFcmToken(request)
    }

    // --- Trip & Offer Management ---
    suspend fun acceptOffer(bookingId: String): Response<ActiveTrip> {
        val requestBody = mapOf("bookingId" to bookingId)
        return RetrofitClient.instance.acceptOffer(requestBody)
    }

    suspend fun rejectOffer(bookingId: String): Response<Unit> {
        val requestBody = mapOf("bookingId" to bookingId)
        return RetrofitClient.instance.rejectOffer(requestBody)
    }

    suspend fun completePickup(bookingId: Int): Response<Unit> {
        // [PERBAIKAN] Mengirim Int sesuai definisi ApiService
        return RetrofitClient.instance.completePickup(bookingId)
    }

    suspend fun completeTrip(tripId: Int): Response<Unit> {
        // [PERBAIKAN] Mengirim Int sesuai definisi ApiService
        return RetrofitClient.instance.completeTrip(tripId)
    }

    // --- Schedule Management ---
    suspend fun getSchedules(): Response<List<Schedule>> {
        // [PERBAIKAN] Memanggil fungsi yang benar dari ApiService
        return RetrofitClient.instance.getAvailableSchedules()
    }

    suspend fun claimSchedule(scheduleId: Int): Response<ActiveTrip> {
        // [PERBAIKAN] Mengirim Map<String, Int> sesuai definisi ApiService
        val requestBody = mapOf("scheduleId" to scheduleId)
        return RetrofitClient.instance.claimSchedule(requestBody)
    }
    suspend fun getAvailableBookings(): Response<List<AvailableOrder>> {
        return RetrofitClient.instance.getAvailableBookings()
    }
}

