package com.example.testmapkit.fragments.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.controllers.SearchController
import com.example.testmapkit.dataModels.Location
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.HistoryResult
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.repositories.RouteResult
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.launch


class RouteViewModel (
    private val routeRepository: RouteRepository,
    private val historyRepository: HistoryRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    // Состояние создания локации
    private val _locationCreationState = MutableLiveData<RouteResult<Location>?>()
    val locationCreationState: LiveData<RouteResult<Location>?> = _locationCreationState

    // Состояние создания маршрута
    private val _routeCreationState = MutableLiveData<RouteResult<Route>?>()
    val routeCreationState: LiveData<RouteResult<Route>?> = _routeCreationState

    private val _routeAddressState = MutableLiveData<List<Route?>?>()
    val routeAddressState: LiveData<List<Route?>?> = _routeAddressState

    private val _routeActiveState = MutableLiveData<List<Route?>?>()
    val routeActiveState: LiveData<List<Route?>?> = _routeActiveState

    private val _currentAddressState = MutableLiveData<List<LocationData?>>()
    val currentAddressState: LiveData<List<LocationData?>> = _currentAddressState

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
            "ViewModel: запуск создания локации для адреса ${location.getAddress()}"
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

    private var searchController = SearchController()

    fun getAddresses(
        point: Point,
        radius: Double
    ) {
        Log.d(
            TAG,
            "ViewModel: запуск поиска рандомной локации"
        )


        _isLoading.value = true

        viewModelScope.launch {
            val result = listOf(
                searchController.getCurrentAddress(point, radius),
                searchController.getRandomAddress(point, radius)
            )
            Log.d(TAG, "Результат поиска текущей локации: ${result[0]?.getAddress()}")
            Log.d(TAG, "Результат поиска рандомной локации: ${result[1]?.getAddress()}")

            _isLoading.value = false
            _currentAddressState.value = result

            if (result[0] != null && result[1] != null) {
                Log.d(TAG, "Финальный текущий адрес: ${result[0]?.getAddress()}")
                Log.d(TAG, "Финальный рандомный адрес: ${result[1]?.getAddress()}")
                _errorMessage.value = null
            } else {
                Log.d(TAG, "Не удалось найти валидный адрес")
            }
        }
    }

    /**
     * Получение списка маршрутов текущего пользователя (асинхронно)
     * с таким же адресом
     * Результат придет в LiveData currentUser
     */
    fun getMyRoutes(
        address: String
    ) {
        Log.d(TAG, "ViewModel: получение списка маршрутов $address")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить список маршрутов")
            _routeAddressState.value = null
            return
        }


        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = historyRepository.getMyRoutes(address = address)
                Log.d(TAG, "Результат получения списка маршрутов: $result")
                _isLoading.value = false

                when (result) {
                    is HistoryResult.Success -> {
                        _routeAddressState.value = result.data
                        Log.d(TAG, "Размер списка маршрутов: ${result.data.size}")
                    }

                    is HistoryResult.Error -> {
                        Log.e(TAG, "Ошибка получения списка маршрутов: ${result.message}")
                        _errorMessage.value = result.message
                        _routeAddressState.value = null
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении списка маршрутов", e)
                _errorMessage.value = e.message ?: "Ошибка получения списка маршрутов"
                _routeAddressState.value = null
                _isLoading.value = false
            }
        }
    }

    /**
     * Очистка ресурсов при уничтожении ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "UserViewModel уничтожен, ресурсы очищены")
        resetStates()
    }

    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Сброс состояний (использовать при уходе с экрана)
     */
    fun resetStates() {
        Log.d(TAG, "Сброс состояний ViewModel")
        _errorMessage.value = null
        _isLoading.value = false
    }
}