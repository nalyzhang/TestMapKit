package com.example.testmapkit.repositories

import android.util.Log
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.Location
import com.example.testmapkit.dataModels.LocationCreateRequest
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.dataModels.RouteCreateRequest
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class RouteResult<out T> {
    data class Success<T>(val data: T) : RouteResult<T>()
    data class Error(val message: String, val code: Int? = null) : RouteResult<Nothing>()
    object Loading : RouteResult<Nothing>()
}

class RouteRepository(
    private val apiService: ApiService
) {

    suspend fun createLocation(
        location: LocationData
    ): RouteResult<Location> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем создание локации: ${location.getAddress()}")

            val request = LocationCreateRequest(
                location.latitude,
                location.longitude,
                location.circleRadius,
                location.getAddress(),
                location.getDateTime()
            )

            val response = apiService.createLocation(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Локация успешно создана, ID: ${response.body()?.id}")
                return@withContext RouteResult.Success(response.body()!!)
            } else {
                val errorMsg = "Ошибка создания локации: ${response.code()}"
                Log.e(TAG, "Ошибка создания локации: $errorMsg")
                return@withContext RouteResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при создании локации: $e")
            return@withContext RouteResult.Error(
                e.message ?: "Неизвестная ошибка при создании локации")
        }
    }


    suspend fun createRoute(
        startLocationID: Int,
        finishLocationID: Int,
        stopLocationID: Int? = null,
        distance: Double,
        time: String,
        date: String
    ): RouteResult<Route> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем создание маршрута")

            val request = RouteCreateRequest(
                startLocationID,
                finishLocationID,
                stopLocationID,
                distance,
                time,
                date
            )

            val response = apiService.createRoute(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Маршрут успешно создан, ID: ${response.body()?.id}")
                return@withContext RouteResult.Success(response.body()!!)
            } else {
                val errorMsg = "Ошибка создания маршрута: ${response.code()}"
                Log.e(TAG, "Ошибка создания маршрута: $errorMsg")
                return@withContext RouteResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при создании маршрута: $e")
            return@withContext RouteResult.Error(
                e.message ?: "Неизвестная ошибка при создании маршрута")
        }
    }
}
