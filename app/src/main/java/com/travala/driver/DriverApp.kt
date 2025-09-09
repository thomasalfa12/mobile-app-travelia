package com.travala.driver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.travala.driver.utils.SessionManager

class DriverApp : Application() {

    companion object {
        const val OFFER_CHANNEL_ID = "new_offer_channel"
    }

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi SessionManager
        SessionManager.init(applicationContext)

        // Buat Notification Channel saat aplikasi dimulai
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tawaran Pesanan Baru"
            val descriptionText = "Notifikasi untuk tawaran pesanan yang masuk"
            val importance = NotificationManager.IMPORTANCE_HIGH // Sangat penting agar muncul di atas
            val channel = NotificationChannel(OFFER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Daftarkan channel ke sistem
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
