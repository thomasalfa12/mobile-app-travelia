package com.travala.driver.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.travala.driver.data.api.AuthInterceptor

object RetrofitClient {

    // PENTING: Ganti dengan URL backend Anda yang sebenarnya!
    // Pastikan diakhiri dengan tanda "/"
    private const val BASE_URL = "https://vital-duly-goat.ngrok-free.app/"

    // by lazy artinya 'instance' hanya akan dibuat saat pertama kali dibutuhkan.
    val instance: ApiService by lazy {
        // 1. Membuat Interceptor untuk Logging
        // Ini sangat berguna untuk debugging, karena akan menampilkan detail request & response di Logcat.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Membuat OkHttpClient dan menambahkan interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor()) // [1] Tambahkan AuthInterceptor
            .addInterceptor(loggingInterceptor) // [2] Logging interceptor sebaiknya terakhir agar bisa log header baru
            .build()

        // 3. Membuat instance Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Untuk mengubah JSON ke objek Kotlin
            .build()

        // 4. Membuat implementasi dari ApiService interface
        retrofit.create(ApiService::class.java)
    }
}