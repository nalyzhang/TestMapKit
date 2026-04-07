package com.example.testmapkit.repositories

import android.util.Log
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.*
import com.example.testmapkit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class FriendResult<out T> {
    data class Success<T>(val data: T) : FriendResult<T>()
    data class Error(val message: String, val code: Int? = null) : FriendResult<Nothing>()
    object Loading : FriendResult<Nothing>()
}

class FriendRepository(
    private val apiService: ApiService
) {

    suspend fun getFriendsList(
    ): FriendResult<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение списка друзей")

            val response = apiService.getFriendsList()

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Успешно получено друзей: ${response.body()?.count}")
                return@withContext FriendResult.Success(response.body()!!.results)
            } else {
                val errorMsg = "Ошибка получения списка друзей: ${response.code()}"
                Log.e(TAG, "Ошибка получения списка друзей: $errorMsg")
                return@withContext FriendResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при получении списка друзей: $e")
            return@withContext FriendResult.Error(
                e.message ?: "Неизвестная ошибка при получении списка друзей")
        }
    }

    suspend fun getUserById(
        userID: Int
    ): FriendResult<UserWithStatistic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение друга по ID $userID")

            val response = apiService.getUserById(userID)

            if (response.isSuccessful && response.body() != null) {
                Log.d(
                    TAG,
                    "Успешно получен профиль друга ${response.body()?.username}"
                )
                return@withContext FriendResult.Success(response.body()!!)
            } else {
                val errorMsg = "Ошибка получения профиля друга по ID $userID: ${response.code()}"
                Log.e(TAG, "Ошибка получения профиля друга по ID $userID: $errorMsg")
                return@withContext FriendResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Неизвестная ошибка при получении профиля друга по ID $userID: $e"
            )
            return@withContext FriendResult.Error(
                e.message ?:
                "Неизвестная ошибка при получении профиля друга по ID $userID"
            )
        }
    }

    suspend fun removeFriend(
        userID: Int
    ): FriendResult<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем удаление друга")

            val response = apiService.removeFriend(userID)

            if (response.isSuccessful) {
                Log.d(TAG, "Друг удален")
                return@withContext FriendResult.Success(Unit)
            } else {
                val errorMsg = "Ошибка удаления друга: ${response.code()}"
                Log.e(TAG, errorMsg)
                return@withContext FriendResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при удалении друга", e)
            return@withContext FriendResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun addFriend(
        userID: Int
    ) : FriendResult<UserWithStatistic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем добавление друга")

            val response = apiService.addFriend(userID)

            if (response.isSuccessful) {
                Log.d(TAG, "Друг ${response.body()!!.username} добавлен")
                return@withContext FriendResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                // Извлекаем только текст ошибки из JSON
                val errorText = if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        // Берем значение по ключу "error"
                        jsonObject.optString("error", errorBody)
                    } catch (e: Exception) {
                        // Если не JSON, возвращаем как есть
                        errorBody
                    }
                } else {
                    "Неизвестная ошибка"
                }
                Log.e(TAG, errorText)
                return@withContext FriendResult.Error(errorText, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при добавлении друга", e)
            return@withContext FriendResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}