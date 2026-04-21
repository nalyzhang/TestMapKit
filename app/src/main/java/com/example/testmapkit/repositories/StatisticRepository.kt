package com.example.testmapkit.repositories

import android.util.Log
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.FriendsStatisticsResponse
import com.example.testmapkit.dataModels.UserStatistic
import com.example.testmapkit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

sealed class StatisticResult<out T> {
    data class Success<T>(val data: T) : StatisticResult<T>()
    data class Error(val message: String, val code: Int? = null) : StatisticResult<Nothing>()
    object Loading : StatisticResult<Nothing>()
}

class StatisticRepository(
    private val apiService: ApiService
) {

    suspend fun getUserStatistic(
        userID: Int,
        dateFrom: String? = null,
        dateTo: String? = null
    ): StatisticResult<UserStatistic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение статистики по ID пользователя $userID")

            val response = apiService.getUserStatistic(userID, dateFrom, dateTo)

            if (response.isSuccessful && response.body() != null) {
                Log.d(
                    TAG,
                    "Успешно получена статистика по ID пользователя"
                )
                return@withContext StatisticResult.Success(response.body()!!)
            } else {
                val errorMsg =
                    "Ошибка получения статистики по ID пользователя $userID: ${response.code()}"
                Log.e(
                    TAG,
                    "Ошибка получения статистики по ID пользователя $userID: $errorMsg")
                return@withContext StatisticResult.Error(
                    errorMsg,
                    response.code()
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Неизвестная ошибка при получении статистики по ID пользователя $userID: $e"
            )
            return@withContext StatisticResult.Error(
                e.message ?:
                "Неизвестная ошибка при получении статистики по ID пользователя $userID"
            )
        }
    }

    suspend fun getMyStatistic(
        dateFrom: String? = null,
        dateTo: String? = null
    ): StatisticResult<UserStatistic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение статистики текущего пользователя")

            val response = apiService.getMyStatistic(dateFrom, dateTo)

            if (response.isSuccessful && response.body() != null) {
                Log.d(
                    TAG,
                    "Успешно получена статистика текущего пользователя"
                )
                return@withContext StatisticResult.Success(response.body()!!)
            } else {
                val errorMsg =
                    "Ошибка получения статистики текущего пользователя: ${response.code()}"
                Log.e(
                    TAG,
                    "Ошибка получения статистики текущего пользователя: $errorMsg")
                return@withContext StatisticResult.Error(
                    errorMsg,
                    response.code()
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Неизвестная ошибка при получении статистики текущего пользователя: $e"
            )
            return@withContext StatisticResult.Error(
                e.message ?:
                "Неизвестная ошибка при получении статистики текущего пользователя"
            )
        }
    }

    suspend fun getFriendsStatistics(
        dateFrom: String? = null,
        dateTo: String? = null
    ): StatisticResult<FriendsStatisticsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение статистик друзей пользователя")

            val response = apiService.getFriendsStatistics(dateFrom, dateTo)

            if (response.isSuccessful && response.body() != null) {
                Log.d(
                    TAG,
                    "Успешно получена статистика друзей пользователя"
                )
                return@withContext StatisticResult.Success(response.body()!!)
            } else {
                val errorMsg =
                    "Ошибка получения статистик друзей пользователя: ${response.code()}"
                Log.e(
                    TAG,
                    "Ошибка получения статистик друзей пользователя: $errorMsg"
                )
                return@withContext StatisticResult.Error(
                    errorMsg,
                    response.code()
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Неизвестная ошибка при получении статистик друзей пользователя: $e"
            )
            return@withContext StatisticResult.Error(
                e.message ?: "Неизвестная ошибка при получении статистик друзей пользователя"
            )
        }
    }
}
