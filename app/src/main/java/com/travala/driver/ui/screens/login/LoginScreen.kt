package com.travala.driver.ui.screens.login

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Listener untuk aksi setelah state berubah
    LaunchedEffect(key1 = uiState) {
        if (uiState.loginSuccess) {
            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Gunakan Crossfade untuk animasi perpindahan antar tahap
        Crossfade(targetState = uiState.currentStep, label = "Login Step Animation") { step ->
            when (step) {
                is LoginStep.PhoneEntry -> PhoneEntryStep(
                    isLoading = uiState.isLoading,
                    onRequestOtp = { phone -> loginViewModel.requestOtp(phone) }
                )
                is LoginStep.OtpVerification -> OtpVerificationStep(
                    isLoading = uiState.isLoading,
                    onVerifyOtp = { otp -> loginViewModel.verifyOtpAndLogin(otp) }
                )
            }
        }
    }
}

@Composable
fun PhoneEntryStep(isLoading: Boolean, onRequestOtp: (String) -> Unit) {
    var whatsappNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login Driver", style = MaterialTheme.typography.headlineMedium)
        Text("Masukkan nomor WhatsApp Anda yang terdaftar untuk menerima kode verifikasi.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("Nomor WhatsApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onRequestOtp(whatsappNumber) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = whatsappNumber.length > 9 // Aktif jika nomor cukup panjang
            ) {
                Text("KIRIM KODE")
            }
        }
    }
}

@Composable
fun OtpVerificationStep(isLoading: Boolean, onVerifyOtp: (String) -> Unit) {
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verifikasi Kode", style = MaterialTheme.typography.headlineMedium)
        Text("Kami telah mengirimkan kode OTP ke WhatsApp Anda. Silakan masukkan di bawah ini.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it }, // Batasi 6 digit
            label = { Text("Kode OTP 6 Digit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onVerifyOtp(otp) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = otp.length == 6 // Aktif jika OTP 6 digit
            ) {
                Text("LOGIN")
            }
        }
    }
}
