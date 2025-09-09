package com.travala.driver.ui.schedule

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travala.driver.data.model.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    onClaimSuccess: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewState.claimResult) {
        when (val result = viewState.claimResult) {
            is ClaimResult.Success -> {
                Toast.makeText(context, "Jadwal berhasil diklaim!", Toast.LENGTH_SHORT).show()
                onClaimSuccess()
            }
            is ClaimResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papan Pekerjaan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewState.schedules.isEmpty()) {
                Text("Tidak ada jadwal tersedia saat ini.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewState.schedules) { schedule ->
                        ScheduleItem(schedule = schedule, onClaim = { viewModel.claimSchedule(schedule.scheduleId) })
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(schedule: Schedule, onClaim: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tujuan: ${schedule.destination}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Filled.CalendarMonth, text = "Waktu Berangkat: ${schedule.departureTime}")
            InfoRow(icon = Icons.Filled.Group, text = "${schedule.passengers} / ${schedule.capacity} Penumpang")
            InfoRow(icon = Icons.Filled.Wallet, text = "Estimasi Pendapatan: Rp ${schedule.estimatedIncome}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClaim,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("KLAIM JADWAL INI")
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
    
