package com.travala.driver.ui.offer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travala.driver.ui.theme.OnlineGreen

@Composable
fun OfferScreen(
    bookingId: String,
    route: String,
    fare: String,
    distance: String,
    onFinish: () -> Unit, // Aksi untuk menutup activity
    viewModel: OfferViewModel = viewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    // Mulai hitung mundur saat screen pertama kali muncul
    LaunchedEffect(Unit) {
        viewModel.startCountdown()
    }

    // Tutup activity jika ada hasil (sukses/gagal/tolak)
    LaunchedEffect(viewState.offerResult) {
        if (viewState.offerResult != null) {
            onFinish()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tawaran Pesanan Baru!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))

                InfoRow("RUTE:", route)
                InfoRow("PENDAPATAN:", "Rp $fare")
                InfoRow("JARAK JEMPUT:", distance)

                Spacer(modifier = Modifier.height(48.dp))

                // Timer
                Text(
                    text = viewState.countdown.toString(),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewState.countdown <= 10) MaterialTheme.colorScheme.error else OnlineGreen
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.rejectOffer(bookingId) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = !viewState.isLoading
                    ) {
                        Text("TOLAK")
                    }
                    Button(
                        onClick = { viewModel.acceptOffer(bookingId) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = !viewState.isLoading
                    ) {
                        Text("TERIMA")
                    }
                }
            }
            if (viewState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}