package com.travala.driver.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.travala.driver.ui.available_orders.AvailableOrdersScreen
import com.travala.driver.ui.screens.dashboard.DashboardScreen
import com.travala.driver.ui.screens.login.LoginScreen
import com.travala.driver.ui.schedule.ScheduleScreen
import com.travala.driver.utils.SessionManager

// Definisikan rute untuk setiap layar
object AppRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val SCHEDULES = "schedules"
    const val AVAILABLE_ORDERS = "available_orders" // <-- [BARU] Rute untuk orderan hari ini
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val startDestination = if (SessionManager.getAuthToken() != null) {
        AppRoutes.DASHBOARD
    } else {
        AppRoutes.LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                onLogout = {
                    SessionManager.clear()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToSchedules = {
                    navController.navigate(AppRoutes.SCHEDULES)
                },
                // [PERBAIKAN] Tambahkan aksi navigasi untuk orderan tersedia
                onNavigateToAvailableOrders = {
                    navController.navigate(AppRoutes.AVAILABLE_ORDERS)
                }
            )
        }

        composable(AppRoutes.SCHEDULES) {
            ScheduleScreen(
                onNavigateBack = { navController.popBackStack() },
                onClaimSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // [BARU] Definisikan layar untuk Daftar Orderan Hari Ini
        composable(AppRoutes.AVAILABLE_ORDERS) {
            AvailableOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onAcceptSuccess = {
                    // Setelah sukses ambil order, kembali ke dashboard
                    // Dashboard akan otomatis update karena TripManager
                    navController.popBackStack()
                }
            )
        }
    }
}

