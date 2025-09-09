package com.travala.driver.data.api

import com.travala.driver.data.model.AvailableOrder
import com.travala.driver.data.model.OtpRequest
import com.travala.driver.data.model.OtpVerifyRequest
import com.travala.driver.data.model.FcmTokenRequest
import com.travala.driver.data.model.LocationUpdateRequest
import com.travala.driver.data.model.LoginRequest
import com.travala.driver.data.model.LoginResponse
import com.travala.driver.data.model.StatusUpdateRequest
import com.travala.driver.ui.screens.dashboard.ActiveTrip
import com.travala.driver.data.model.Schedule
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    /**
     * Endpoint untuk melakukan login driver.
     * Mengirimkan nomor WhatsApp dan menerima detail driver beserta token.
     */
    @POST("api/drivers/login/request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): Response<Unit>
    @POST("api/drivers/login/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<LoginResponse>
    @POST("api/drivers/status")
    suspend fun updateStatus(@Body request: StatusUpdateRequest): Response<Unit> // Response tidak butuh body
    @POST("api/drivers/location")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<Unit>
    @POST("api/drivers/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<Unit>
    @POST("api/trips/accept")
    suspend fun acceptOffer(@Body request: Map<String, String>): Response<ActiveTrip>
    @POST("api/trips/reject")
    suspend fun rejectOffer(@Body request: Map<String, String>): Response<Unit>
    @POST("api/bookings/{bookingId}/complete-pickup")
    suspend fun completePickup(@Path("bookingId") bookingId: Int): Response<Unit>
    @POST("api/trips/{tripId}/complete")
    suspend fun completeTrip(@Path("tripId") tripId: Int): Response<Unit>
    @GET("api/schedules")
    suspend fun getAvailableSchedules(): Response<List<Schedule>>
    @POST("api/schedules/claim")
    suspend fun claimSchedule(@Body request: Map<String, Int>): Response<ActiveTrip>
    @GET("api/bookings/available")
    suspend fun getAvailableBookings(): Response<List<AvailableOrder>>

}