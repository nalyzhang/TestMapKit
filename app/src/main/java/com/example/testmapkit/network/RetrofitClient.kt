// RetrofitClient.kt - ИСПРАВЛЕННАЯ ВЕРСИЯ
package com.example.testmapkit.network

import com.example.testmapkit.BASE_URL_IP
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor(private val tokenManager: TokenManager) {

    companion object {
        private const val BASE_URL = BASE_URL_IP

        @Volatile
        private var instance: RetrofitClient? = null

        fun getInstance(tokenManager: TokenManager): RetrofitClient {
            return instance ?: synchronized(this) {
                instance ?: RetrofitClient(tokenManager).also { instance = it }
            }
        }
    }

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            android.util.Log.d("RetrofitClient", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")

                // КРИТИЧЕСКИ ВАЖНО: берем токен динамически каждый раз
                val currentToken = tokenManager.getToken()
                if (!currentToken.isNullOrEmpty()) {
                    android.util.Log.d("RetrofitClient", "Добавляем токен: ${currentToken.take(10)}...")
                    requestBuilder.header("Authorization", "Token $currentToken")
                } else {
                    android.util.Log.d("RetrofitClient", "Токен отсутствует")
                }

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}