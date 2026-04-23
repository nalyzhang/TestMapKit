package com.example.testmapkit.fragments.statistic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.FriendsStatisticsResponse
import com.example.testmapkit.dataModels.UserStatistic
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.StatisticRepository
import com.example.testmapkit.repositories.StatisticResult
import kotlinx.coroutines.launch

class StatisticViewModel (
    private val statisticRepository: StatisticRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    private val _friendsStatistic = MutableLiveData<FriendsStatisticsResponse?>()
    val friendsStatistic: LiveData<FriendsStatisticsResponse?> = _friendsStatistic

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _statisticByID = MutableLiveData<UserStatistic?>()
    val statisticByID: LiveData<UserStatistic?> = _statisticByID

    private val _myStatistic = MutableLiveData<UserStatistic?>()
    val myStatistic: LiveData<UserStatistic?> = _myStatistic

    /**
     * Получение статистики друзей текущего пользователя (асинхронно)
     * Результат придет в LiveData currentUser
     */
    fun getFriendsStatistics(
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        Log.d(TAG, "ViewModel: получение статистики друзей")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить статистику друзей")
            _friendsStatistic.value = null
            return
        }


        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = statisticRepository.getFriendsStatistics(dateFrom, dateTo)
                Log.d(TAG, "Результат получения статистики друзей: $result")
                _isLoading.value = false

                when (result) {
                    is StatisticResult.Success -> {
                        _friendsStatistic.value = result.data
                        Log.d(TAG, "Размер статистики друзей: ${result.data.count}")
                    }
                    is StatisticResult.Error -> {
                        Log.e(TAG, "Ошибка получения статистики друзей: ${result.message}")
                        _errorMessage.value = result.message
                        _friendsStatistic.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении статистики друзей", e)
                _errorMessage.value = e.message ?: "Ошибка получения статистики друзей"
                _friendsStatistic.value = null
                _isLoading.value = false
            }
        }
    }

    /**
     * Получение статистики пользователя по ID
     */
    fun getUserStatistic(
        userID: Int,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        Log.d(TAG, "ViewModel: получение статистики пользователя $userID")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить статистику пользователя $userID")
            _statisticByID.value = null
            return
        }

        viewModelScope.launch {
            try {
                val result = statisticRepository.getUserStatistic(userID, dateFrom, dateTo)
                Log.d(TAG, "Результат получения статистики пользователя $userID: $result")

                when (result) {
                    is StatisticResult.Success -> {
                        _statisticByID.value = result.data
                        Log.d(TAG, "Статистика пользователя $userID")
                    }
                    is StatisticResult.Error -> {
                        Log.e(
                            TAG,
                            "Ошибка получения статистики пользователя: ${result.message}"
                        )
                        _errorMessage.value = result.message
                        _statisticByID.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Исключение при получении статистики пользователя",
                    e)
                _errorMessage.value = e.message ?: "Ошибка получения статистики пользователя"
                _statisticByID.value = null
            }
        }
    }

    /**
     * Получение статистики текущего пользователя
     */
    fun getMyStatistic(
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        Log.d(TAG, "ViewModel: получение статистики текущего пользователя")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить статистику текущего пользователя")
            _myStatistic.value = null
            return
        }

        viewModelScope.launch {
            try {
                val result = statisticRepository.getMyStatistic(dateFrom, dateTo)
                Log.d(TAG, "Результат получения статистики текущего пользователя: $result")

                when (result) {
                    is StatisticResult.Success -> {
                        _myStatistic.value = result.data
                        Log.d(TAG, "Статистика текущего пользователя")
                    }
                    is StatisticResult.Error -> {
                        Log.e(
                            TAG,
                            "Ошибка получения статистики пользователя: ${result.message}"
                        )
                        _errorMessage.value = result.message
                        _myStatistic.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Исключение при получении статистики пользователя",
                    e)
                _errorMessage.value = e.message ?: "Ошибка получения статистики пользователя"
                _myStatistic.value = null
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