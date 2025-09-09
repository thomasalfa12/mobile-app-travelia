package com.travala.driver.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SessionManager {

    private const val PREFS_NAME = "driver_secure_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_DRIVER_ID = "driver_id"
    private const val KEY_DRIVER_NAME = "driver_name"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        // [1] Buat atau dapatkan master key untuk enkripsi
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // [2] Buat instance EncryptedSharedPreferences
        prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Fungsi-fungsi di bawah ini tidak perlu diubah sama sekali
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveDriverInfo(driverId: Int, name: String) {
        prefs.edit()
            .putInt(KEY_DRIVER_ID, driverId)
            .putString(KEY_DRIVER_NAME, name)
            .apply()
    }

    fun getDriverId(): Int {
        return prefs.getInt(KEY_DRIVER_ID, -1)
    }

    fun getDriverName(): String? {
        return prefs.getString(KEY_DRIVER_NAME, null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}