// TokenManager.kt
package com.example.testmapkit.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {

    companion object {
        private const val PREF_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
        android.util.Log.d("TokenManager", "Токен сохранен")
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        android.util.Log.d("TokenManager", "Получен токен: ${token?.take(10)}...")
        return token
    }

    fun hasToken(): Boolean {
        val has = !getToken().isNullOrEmpty()
        android.util.Log.d("TokenManager", "Наличие токена: $has")
        return has
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
        android.util.Log.d("TokenManager", "Токен удален")
    }
}