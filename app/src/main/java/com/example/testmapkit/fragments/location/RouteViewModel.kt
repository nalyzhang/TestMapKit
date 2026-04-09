package com.example.testmapkit.fragments.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.Location
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.repositories.RouteResult
import kotlinx.coroutines.launch


class RouteViewModel (
    private val routeRepository: RouteRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    // Состояние создания локации
    private val _locationCreationState = MutableLiveData<RouteResult<Location>?>()
    val locationCreationState: LiveData<RouteResult<Location>?> = _locationCreationState

    // Состояние создания маршрута
    private val _routeCreationState = MutableLiveData<RouteResult<Route>?>()
    val routeCreationState: LiveData<RouteResult<Route>?> = _routeCreationState

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Создание новой локации
     */
    fun createLocation(
        location: LocationData,
        time: String
    ) {
        Log.d(
            TAG,
            "ViewModel: запуск создания локации для адреса ${location.getAddressLine()}"
        )

        _isLoading.value = true
        _locationCreationState.value = RouteResult.Loading

        viewModelScope.launch {
            val result = routeRepository.createLocation(
                location, time)
            Log.d(TAG, "Результат создания локации: $result")

            _locationCreationState.value = result
            _isLoading.value = false

            when (result) {
                is RouteResult.Success -> {
                    Log.d(
                        TAG,
                        "Локация создана успешна, адрес: ${result.data.address}")
                    _errorMessage.value = null
                }
                is RouteResult.Error -> {
                    Log.e(TAG, "Ошибка создания локации: ${result.message}")
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Создание нового маршрута
     */
    fun createRoute(
        startLocationID: Int,
        finishLocationID: Int,
        distance: Double,
        time: String,
        date: String
    ) {
        Log.d(
            TAG,
            "ViewModel: запуск создания маршрута"
        )

        _isLoading.value = true
        _routeCreationState.value = RouteResult.Loading

        viewModelScope.launch {
            val result = routeRepository.createRoute(
                startLocationID,
                finishLocationID,
                distance,
                time,
                date)
            Log.d(TAG, "Результат создания маршрута: $result")

            _routeCreationState.value = result
            _isLoading.value = false

            when (result) {
                is RouteResult.Success -> {
                    Log.d(
                        TAG,
                        "Маршрут создан успешно, ID: ${result.data.id}")
                    _errorMessage.value = null
                }
                is RouteResult.Error -> {
                    Log.e(TAG, "Ошибка создания маршрута: ${result.message}")
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }
}