package com.travala.driver.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.travala.driver.DriverApp
import com.travala.driver.R
import com.travala.driver.data.repository.AuthRepository
import com.travala.driver.ui.offer.OfferActivity
import com.travala.driver.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val authRepository = AuthRepository()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_DEBUG", "Token baru dibuat: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val driverId = SessionManager.getDriverId()
        if (driverId != -1) {
            serviceScope.launch {
                try {
                    authRepository.registerFcmToken(driverId, token)
                    Log.d("FCM_DEBUG", "Token berhasil dikirim ke server.")
                } catch (e: Exception) {
                    Log.e("FCM_DEBUG", "Gagal mengirim token ke server: ", e)
                }
            }
        } else {
            Log.w("FCM_DEBUG", "Driver ID tidak ditemukan, token akan dikirim saat login berikutnya.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [DEBUG] Langkah pertama: Cek apakah pesan sampai di sini
        Log.d("FCM_DEBUG", ">>> PESAN FCM DITERIMA! <<<")
        Log.d("FCM_DEBUG", "Dari: ${remoteMessage.from}")

        // [DEBUG] Langkah kedua: Cek isi datanya
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_DEBUG", "Data Payload: ${remoteMessage.data}")

            val bookingId = remoteMessage.data["bookingId"] ?: ""
            val rute = remoteMessage.data["route"] ?: "Rute tidak tersedia"
            val tarif = remoteMessage.data["fare"] ?: "0"
            val jarak = remoteMessage.data["distance"] ?: "0 km"

            if (bookingId.isBlank()) {
                Log.e("FCM_DEBUG", "Pesan diterima tapi bookingId kosong. Notifikasi tidak ditampilkan.")
                return
            }

            sendOfferNotification(bookingId, rute, tarif, jarak)
        } else {
            Log.w("FCM_DEBUG", "Pesan FCM diterima tapi tidak ada data payload.")
        }

        // Cek jika notifikasi dikirim via 'notification payload' (untuk debug)
        remoteMessage.notification?.let {
            Log.d("FCM_DEBUG", "Notification Payload: ${it.body}")
        }
    }

    private fun sendOfferNotification(bookingId: String, rute: String, tarif: String, jarak: String) {
        val intent = Intent(this, OfferActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_BOOKING_ID", bookingId)
            putExtra("EXTRA_ROUTE", rute)
            putExtra("EXTRA_FARE", tarif)
            putExtra("EXTRA_DISTANCE", jarak)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, DriverApp.OFFER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti ikon
            .setContentTitle("Tawaran Pesanan Baru!")
            .setContentText(rute)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Pendapatan: Rp $tarif\nJarak Jemput: $jarak")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationId = System.currentTimeMillis().toInt()
        try {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
            Log.d("FCM_DEBUG", "Notifikasi berhasil ditampilkan untuk bookingId: $bookingId.")
        } catch (e: SecurityException) {
            Log.e("FCM_DEBUG", "Gagal menampilkan notifikasi karena izin tidak ada.", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
