package com.example.testmapkit.fragments.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.HistoryResult
import kotlinx.coroutines.launch

class HistoryViewModel (
    private val historyRepository: HistoryRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    private val _historyList = MutableLiveData<List<Route>?>()
    val historyList: LiveData<List<Route>?> = _historyList

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _routeByID = MutableLiveData<Route?>()
    val routeByID: LiveData<Route?> = _routeByID

    private val _removeRouteState = MutableLiveData<HistoryResult<Unit>?>()
    val removeRouteState: LiveData<HistoryResult<Unit>?> = _removeRouteState


    /**
     * Получение списка маршрутов текущего пользователя (асинхронно)
     * Результат придет в LiveData currentUser
     */
    fun getMyRoutes() {
        Log.d(TAG, "ViewModel: получение списка маршрутов")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить список маршрутов")
            _historyList.value = null
            return
        }


        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = historyRepository.getMyRoutes()
                Log.d(TAG, "Результат получения списка маршрутов: $result")
                _isLoading.value = false

                when (result) {
                    is HistoryResult.Success -> {
                        _historyList.value = result.data
                        Log.d(TAG, "Размер списка маршрутов: ${result.data.size}")
                    }

                    is HistoryResult.Error -> {
                        Log.e(TAG, "Ошибка получения списка маршрутов: ${result.message}")
                        _errorMessage.value = result.message
                        _historyList.value = null
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении списка маршрутов", e)
                _errorMessage.value = e.message ?: "Ошибка получения списка маршрутов"
                _historyList.value = null
                _isLoading.value = false
            }
        }
    }

    /**
     * Получение маршрута пользователя по ID
     */
    fun getRouteById(
        routeID: Int
    ) {
        Log.d(TAG, "ViewModel: получение маршрута $routeID")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить маршрут $routeID")
            _routeByID.value = null
            return
        }

        viewModelScope.launch {
            try {
                val result = historyRepository.getRouteById(routeID)
                Log.d(TAG, "Результат получения маршрута $routeID: $result")

                when (result) {
                    is HistoryResult.Success -> {
                        _routeByID.value = result.data
                        Log.d(TAG, "Пользователь $routeID: ${result.data.id}")
                    }
                    is HistoryResult.Error -> {
                        Log.e(
                            TAG,
                            "Ошибка получения маршрута $routeID: ${result.message}"
                        )
                        _errorMessage.value = result.message
                        _routeByID.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении маршрута $routeID", e)
                _errorMessage.value = e.message ?: "Ошибка получения данных маршрута $routeID"
                _routeByID.value = null
            }
        }
    }

    /**
     * Удаление маршрута
     */
    fun deleteRoute(
        routeID: Int
    ) {
        Log.d(TAG, "ViewModel: удаление маршрута")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно удалить маршрут $routeID")
            _routeByID.value = null
            return
        }

        _removeRouteState.value = HistoryResult.Loading

        viewModelScope.launch {
            try {
                val result = historyRepository.deleteRoute(routeID)
                Log.d(TAG, "Результат удаления маршрута: $result")

                when (result) {
                    is HistoryResult.Success -> {
                        _removeRouteState.value = HistoryResult.Success(Unit)
                        Log.d(TAG, "Маршрут успешно удален")
                        _errorMessage.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при удалении маршрута", e)
                _removeRouteState.value = HistoryResult.Error(
                    e.message ?: "Ошибка удаления маршрута")
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