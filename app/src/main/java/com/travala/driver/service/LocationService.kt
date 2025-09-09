package com.travala.driver.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.travala.driver.R
import com.travala.driver.data.model.LocationUpdateRequest
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.utils.SessionManager
import kotlinx.coroutines.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val authRepository = AuthRepository()

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "LocationServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Agent Travel Unsri")
            .setContentText("Anda sedang ONLINE. Lokasi sedang dilacak.")
            .setSmallIcon(R.mipmap.ic_launcher) // Ganti dengan ikon notifikasi Anda
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
        Log.d("LocationService", "Service started and tracking location.")
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf() // Hentikan service jika izin tidak ada
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000L) // 30 detik
            .setMinUpdateIntervalMillis(15000L) // 15 detik
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d("LocationService", "Location update: ${location.latitude}, ${location.longitude}")
                    serviceScope.launch {
                        val driverId = SessionManager.getDriverId()
                        if (driverId != -1) {
                            val request = LocationUpdateRequest(driverId, location.latitude, location.longitude)
                            try {
                                authRepository.updateLocation(request)
                            } catch (e: Exception) {
                                Log.e("LocationService", "Failed to send location: ${e.message}")
                            }
                        }
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        Log.d("LocationService", "Service stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(serviceChannel)
        }
    }
}