package com.travala.driver.ui.screens.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travala.driver.services.LocationService
import com.travala.driver.ui.offer.OfferActivity
import com.travala.driver.ui.theme.DarkGray
import com.travala.driver.ui.theme.OnlineGreen
import com.travala.driver.utils.SessionManager
import kotlinx.coroutines.launch

// =================================================================
// MAIN SCREEN & SCAFFOLD
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAvailableOrders: () -> Unit
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val viewState by viewModel.viewState.collectAsState()
    val snackbarOffer by viewModel.newOfferSnackbar.collectAsState()
    val context = LocalContext.current
    val driverName = SessionManager.getDriverName() ?: "Driver"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Efek untuk menampilkan Snackbar saat ada tawaran baru
    LaunchedEffect(snackbarOffer) {
        snackbarOffer?.let { offer ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = offer.text,
                    actionLabel = "LIHAT",
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    val intent = Intent(context, OfferActivity::class.java).apply {
                        putExtra("EXTRA_BOOKING_ID", offer.bookingId)
                        // TODO: Kirim data lain yang relevan (route, fare, dll.)
                    }
                    context.startActivity(intent)
                }
                viewModel.dismissSnackbarOffer()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.setOnlineStatus(true)
            else Toast.makeText(context, "Izin lokasi dibutuhkan untuk bisa ONLINE.", Toast.LENGTH_LONG).show()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.serviceEvent.collect { action ->
            val serviceIntent = Intent(context, LocationService::class.java)
            when (action) {
                DashboardViewModel.ServiceAction.START -> ContextCompat.startForegroundService(context, serviceIntent)
                DashboardViewModel.ServiceAction.STOP -> context.stopService(serviceIntent)
            }
        }
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (dashboardState is DashboardState.Offline) DarkGray else MaterialTheme.colorScheme.surface,
        label = "backgroundColor"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopAppBar(
                state = dashboardState,
                driverName = driverName,
                onLogout = onLogout,
                onCancelTrip = { viewModel.forceFinishTrip() }
            )
        },
        containerColor = animatedBackgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = dashboardState) {
                is DashboardState.Offline -> OfflineUi(
                    isLoading = viewState.isLoading,
                    onToggleOnline = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
                is DashboardState.Searching -> SearchingUi(
                    isLoading = viewState.isLoading,
                    onToggleOffline = { viewModel.setOnlineStatus(false) },
                    onNavigateToSchedules = onNavigateToSchedules,
                    // [BARU] Tambahkan aksi untuk navigasi ke daftar orderan
                    onNavigateToAvailableOrders = { /* TODO: Navigasi ke layar baru */ }
                )
                is DashboardState.InTrip -> InTripUi(
                    trip = state.trip,
                    onFinishTrip = { viewModel.finishTrip() },
                    viewModel = viewModel
                )
            }
        }
    }
}

// =================================================================
// DYNAMIC TOP APP BAR
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    state: DashboardState,
    driverName: String,
    onLogout: () -> Unit,
    onCancelTrip: () -> Unit
) {
    val title = when (state) {
        is DashboardState.Offline -> "Mode Offline"
        is DashboardState.Searching -> "Halo, $driverName!"
        is DashboardState.InTrip -> "Mengelola Perjalanan"
    }
    val containerColor by animateColorAsState(
        targetValue = if (state is DashboardState.Offline) DarkGray else MaterialTheme.colorScheme.surface,
        label = "topbarColor"
    )
    val titleContentColor = if (state is DashboardState.Offline) Color.White else MaterialTheme.colorScheme.onSurface

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleContentColor
        ),
        actions = {
            if (state is DashboardState.InTrip) {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opsi")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Batalkan Perjalanan") }, onClick = {
                        onCancelTrip()
                        menuExpanded = false
                    })
                }
            } else {
                TextButton(onClick = onLogout) { Text("Logout") }
            }
        }
    )
}


// =================================================================
// COMPOSABLE UNTUK SETIAP STATE (FINAL)
// =================================================================

@Composable
fun OfflineUi(isLoading: Boolean, onToggleOnline: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PowerSettingsNew,
            contentDescription = "Offline",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Anda Sedang Offline",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text("Mulai bekerja untuk menerima pesanan dan melihat pendapatan.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            FilledTonalButton(
                onClick = onToggleOnline,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Mulai Bekerja (Go Online)", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchingUi(
    isLoading: Boolean,
    onToggleOffline: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToAvailableOrders: () -> Unit // [BARU] Terima aksi navigasi
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Kartu Status & Pendapatan ---
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(16.dp)) {
                    Text("STATUS", style = MaterialTheme.typography.labelMedium)
                    Text("ONLINE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = OnlineGreen)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(16.dp)) {
                    Text("PENDAPATAN", style = MaterialTheme.typography.labelMedium)
                    Text("Rp 0", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- [REFACTOR] PUSAT ORDER ---
        Text("Pusat Order", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))

        // Kartu untuk Orderan Hari Ini
        OutlinedCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToAvailableOrders) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkHistory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Daftar Orderan Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Lihat & ambil orderan on-the-spot yang tersedia.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Kartu untuk Jadwal Besok
        OutlinedCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToSchedules) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Papan Pekerjaan (Besok)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Klaim jadwal perjalanan untuk hari berikutnya.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Go Offline
        Button(
            onClick = onToggleOffline,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Berhenti Bekerja (Go Offline)")
        }
        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

enum class TaskState { COMPLETED, CURRENT, UPCOMING }

@Composable
fun InTripUi(trip: ActiveTrip, onFinishTrip: () -> Unit, viewModel: DashboardViewModel) {
    val activity = (LocalContext.current as? Activity)

    // Cegah supir keluar dari aplikasi, tapi minimalkan aplikasi
    BackHandler(enabled = true) {
        activity?.moveTaskToBack(true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 80.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("PERJALANAN AKTIF", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Tujuan Akhir: ${trip.finalDestination}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (trip.totalCapacity - trip.remainingCapacity).toFloat() / trip.totalCapacity },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${trip.remainingCapacity} dari ${trip.totalCapacity} Kursi Tersisa", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            val firstUpcomingTask = trip.tasks.firstOrNull { !it.isCompleted }
            itemsIndexed(trip.tasks) { index, task ->
                val state = when {
                    task.isCompleted -> TaskState.COMPLETED
                    task == firstUpcomingTask -> TaskState.CURRENT
                    else -> TaskState.UPCOMING
                }
                PickupTaskItem(taskState = state, task = task, taskNumber = index + 1, viewModel = viewModel)
            }
        }

        Surface(modifier = Modifier.align(Alignment.BottomCenter), shadowElevation = 8.dp) {
            Button(
                onClick = onFinishTrip,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                enabled = trip.tasks.all { it.isCompleted }
            ) {
                Text("SELESAIKAN PERJALANAN", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupTaskItem(taskState: TaskState, task: PickupTask, taskNumber: Int, viewModel: DashboardViewModel) {
    // [PERBAIKAN] Panggil TaskContent secara langsung di dalam setiap Card
    when(taskState) {
        TaskState.CURRENT -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                TaskContent(task = task, taskState = taskState, taskNumber = taskNumber, viewModel = viewModel)
            }
        }
        TaskState.UPCOMING -> {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                TaskContent(task = task, taskState = taskState, taskNumber = taskNumber, viewModel = viewModel)
            }
        }
        TaskState.COMPLETED -> {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                TaskContent(task = task, taskState = taskState, taskNumber = taskNumber, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TaskContent(task: PickupTask, taskState: TaskState, taskNumber: Int, viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val contentColor = when(taskState) {
        TaskState.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        TaskState.CURRENT -> MaterialTheme.colorScheme.onPrimaryContainer
        TaskState.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PENJEMPUTAN #${taskNumber}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (taskState == TaskState.CURRENT) MaterialTheme.colorScheme.primary else contentColor
            )
            Spacer(modifier = Modifier.weight(1f))
            if (taskState == TaskState.COMPLETED) {
                Icon(Icons.Filled.CheckCircle, "Selesai", tint = OnlineGreen)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = task.passengerName,
            style = MaterialTheme.typography.titleLarge,
            textDecoration = if (taskState == TaskState.COMPLETED) TextDecoration.LineThrough else null,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, "Lokasi", Modifier.size(16.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.location,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (taskState == TaskState.COMPLETED) TextDecoration.LineThrough else null,
                color = contentColor
            )
        }

        if (taskState == TaskState.CURRENT) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${task.location}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Google Maps tidak terpasang.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Navigation, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NAVIGASI")
                    }
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${task.passengerWaNumber}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp tidak terpasang.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Phone, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HUBUNGI")
                    }
                }
                Button(
                    onClick = { viewModel.completePickupTask(task.bookingId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                ) {
                    Text("SUDAH DIJEMPUT")
                }
            }
        }
    }
}

