package com.travala.driver.ui.available_orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travala.driver.data.model.AvailableOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableOrdersScreen(
    onNavigateBack: () -> Unit,
    onAcceptSuccess: () -> Unit,
    viewModel: AvailableOrdersViewModel = viewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewState.error) {
        viewState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(viewState.claimSuccess) {
        if (viewState.claimSuccess) {
            Toast.makeText(context, "Orderan berhasil diambil!", Toast.LENGTH_SHORT).show()
            onAcceptSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orderan Tersedia Hari Ini") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
            } else if (viewState.orders.isEmpty()) {
                Text("Tidak ada orderan tersedia saat ini.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewState.orders) { order ->
                        AvailableOrderItem(order = order, onAcceptClick = {
                            viewModel.acceptAvailableOrder(order.bookingId)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableOrderItem(order: AvailableOrder, onAcceptClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(order.route, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            InfoRow(icon = Icons.Default.LocationOn, text = order.pickupPoint)

            Row {
                InfoRow(icon = Icons.Default.Wallet, text = "Rp ${order.fare}", modifier = Modifier.weight(1f))
                InfoRow(icon = Icons.Default.Group, text = "${order.passengerCount} Penumpang", modifier = Modifier.weight(1f))
            }

            Button(
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("AMBIL ORDERAN INI")
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
