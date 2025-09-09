package com.travala.driver.data.model

// Mewakili satu item jadwal yang bisa diklaim oleh supir
data class Schedule(
    val scheduleId: Int,
    val destination: String,
    val departureTime: String, // Dibuat String agar mudah ditampilkan
    val estimatedIncome: Int,
    val passengers: Int,
    val capacity: Int
)
    
