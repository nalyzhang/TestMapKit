package com.example.testmapkit.repositories

import android.util.Log
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class HistoryResult<out T> {
    data class Success<T>(val data: T) : HistoryResult<T>()
    data class Error(val message: String, val code: Int? = null) : HistoryResult<Nothing>()
    object Loading : HistoryResult<Nothing>()
}

class HistoryRepository(
    private val apiService: ApiService
) {
    suspend fun getMyRoutes(
        address: String? = null
    ): HistoryResult<List<Route>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение списка маршрутов")

            val response = apiService.getMyRoutes(address)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Успешно получено маршрутов: ${response.body()?.count}")
                return@withContext HistoryResult.Success(response.body()!!.results)
            } else {
                val errorMsg = "Ошибка получения списка маршрутов: ${response.code()}"
                Log.e(TAG, "Ошибка получения списка маршрутов: $errorMsg")
                return@withContext HistoryResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при получении списка маршрутов: $e")
            return@withContext HistoryResult.Error(
                e.message ?: "Неизвестная ошибка при получении списка маршрутов")
        }
    }

    suspend fun getRouteById(
        routeID: Int
    ): HistoryResult<Route> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение маршрута по ID $routeID")

            val response = apiService.getRouteById(routeID)

            if (response.isSuccessful && response.body() != null) {
                Log.d(
                    TAG,
                    "Успешно получен маршрут ${response.body()?.id}"
                )
                return@withContext HistoryResult.Success(response.body()!!)
            } else {
                val errorMsg = "Ошибка получения маршрута по ID $routeID: ${response.code()}"
                Log.e(TAG, "Ошибка получения маршрута по ID $routeID: $errorMsg")
                return@withContext HistoryResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Неизвестная ошибка при получении маршрута по ID $routeID: $e"
            )
            return@withContext HistoryResult.Error(
                e.message ?:
                "Неизвестная ошибка при получении маршрута по ID $routeID"
            )
        }
    }

    suspend fun deleteRoute(
        routeID: Int
    ): HistoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем удаление маршрута")

            val response = apiService.deleteRoute(routeID)

            if (response.isSuccessful) {
                Log.d(TAG, "Маршрут удален")
                return@withContext HistoryResult.Success(Unit)
            } else {
                val errorMsg = "Ошибка удаления маршрута: ${response.code()}"
                Log.e(TAG, errorMsg)
                return@withContext HistoryResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при удалении маршрута", e)
            return@withContext HistoryResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}