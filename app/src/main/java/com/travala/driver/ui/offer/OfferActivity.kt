package com.travala.driver.ui.offer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.travala.driver.ui.theme.TravalaTheme

class OfferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengambil data yang dikirim dari notifikasi FCM
        val bookingId = intent.getStringExtra("EXTRA_BOOKING_ID") ?: ""
        val route = intent.getStringExtra("EXTRA_ROUTE") ?: "Rute tidak tersedia"
        val fare = intent.getStringExtra("EXTRA_FARE") ?: "0"
        val distance = intent.getStringExtra("EXTRA_DISTANCE") ?: "0 km"

        setContent {
            TravalaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Panggil OfferScreen dengan data yang sudah diambil
                    OfferScreen(
                        bookingId = bookingId,
                        route = route,
                        fare = fare,
                        distance = distance,
                        onFinish = { finish() } // Memberi tahu Composable cara menutup Activity ini
                    )
                }
            }
        }
    }
}
