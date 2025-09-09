package com.travala.driver.data.api

import com.travala.driver.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Ambil request asli
        val originalRequest = chain.request()

        // Ambil token dari SessionManager
        val token = SessionManager.getAuthToken()

        // Jika token ada, tambahkan header Authorization
        val requestBuilder = originalRequest.newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        val newRequest = requestBuilder.build()

        // Lanjutkan request dengan header baru (jika ada)
        return chain.proceed(newRequest)
    }
}