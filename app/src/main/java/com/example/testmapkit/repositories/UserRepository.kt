package com.example.testmapkit.repositories

import android.util.Log
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.*
import com.example.testmapkit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class UserResult<out T> {
    data class Success<T>(val data: T) : UserResult<T>()
    data class Error(val message: String, val code: Int? = null) : UserResult<Nothing>()
    object Loading : UserResult<Nothing>()
}

class UserRepository(
    private val apiService: ApiService
) {

    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): UserResult<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем регистрацию: $username, $email")

            val request = RegisterRequest(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )

            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Регистрация успешна, пользователь ID: ${response.body()?.id}")
                return@withContext UserResult.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Некорректные данные. Проверьте введенную информацию"
                    409 -> "Пользователь с таким email или username уже существует"
                    else -> "Ошибка регистрации: ${response.code()}"
                }
                Log.e(TAG, "Ошибка регистрации: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при регистрации: $e")
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при регистрации")
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): UserResult<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем вход: $email")

            val request = LoginRequest(email = email, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.authToken
                Log.d(TAG, "Вход успешен, токен получен: ${token.take(10)}...")
                return@withContext UserResult.Success(token)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Неверный логин или пароль"
                    else -> "Ошибка входа: ${response.code()}"
                }
                Log.e(TAG, "Ошибка входа: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при входе", e)
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при входе")
        }
    }

    suspend fun logout(
    ): UserResult<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем выход")

            val response = apiService.logout()

            if (response.isSuccessful) {
                Log.d(TAG, "Выход прошел успешно")
                return@withContext UserResult.Success(Unit)
            } else {
                val errorMsg = "Ошибка выхода: ${response.code()}"
                Log.e(TAG, "Ошибка выхода: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при выходе", e)
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при выходе")
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): UserResult<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем изменение пароля")

            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            val response = apiService.changePassword(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Пароль успешно изменен на $newPassword")
                return@withContext UserResult.Success(Unit)
            } else {
                val errorMsg = "Ошибка изменения пароля: ${response.code()}"
                Log.e(TAG, "Ошибка изменения пароля: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при изменении пароля", e)
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при изменении пароля")
        }
    }

    suspend fun getCurrentUser(
    ): UserResult<UserWithStatistic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем получение текущего пользователя")

            val response = apiService.getCurrentUser()

            if (response.isSuccessful) {
                Log.d(TAG, "Текущий пользователь успешно получен")
                val userWithStatistic = response.body()!!
                return@withContext UserResult.Success(userWithStatistic)
            } else {
                val errorMsg = "Ошибка получения текущего пользователя: ${response.code()}"
                Log.e(TAG, "Ошибка получения текущего пользователя: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при получении текущего пользователя", e)
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при получении текущего пользователя")
        }
    }

    suspend fun putCurrentUser(
        username: String,
        email: String,
        firstName: String,
        lastName: String
    ): UserResult<UserUpdate> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем изменение текущего пользователя")

            val request = UserUpdate(username, email, firstName, lastName)

            val response = apiService.putCurrentUser(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Текущий пользователь успешно изменен")
                val updateUser = response.body()!!
                return@withContext UserResult.Success(updateUser)
            } else {
                val errorMsg = "Ошибка изменения текущего пользователя: ${response.code()}"
                Log.e(TAG, "Ошибка изменения текущего пользователя: $errorMsg")
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при изменении текущего пользователя", e)
            return@withContext UserResult.Error(
                e.message ?: "Неизвестная ошибка при изменении текущего пользователя")
        }
    }

    suspend fun updateAvatar(
        base64Image: String
    ): UserResult<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем обновление аватара")

            val request = AvatarUpdateRequest(avatarBase64 = base64Image)
            val response = apiService.updateAvatar(request)

            if (response.isSuccessful && response.body() != null) {
                val avatarUrl = response.body()!!.avatarUrl
                Log.d(TAG, "Аватар обновлен: $avatarUrl")
                return@withContext UserResult.Success(avatarUrl)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Неверный формат изображения"
                    401 -> "Не авторизован"
                    else -> "Ошибка обновления аватара: ${response.code()}"
                }
                Log.e(TAG, errorMsg)
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при обновлении аватара", e)
            return@withContext UserResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    /**
     * Удаление аватара
     */
    suspend fun deleteAvatar(): UserResult<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем удаление аватара")

            val response = apiService.deleteAvatar()

            if (response.isSuccessful) {
                Log.d(TAG, "Аватар удален")
                return@withContext UserResult.Success(Unit)
            } else {
                val errorMsg = "Ошибка удаления аватара: ${response.code()}"
                Log.e(TAG, errorMsg)
                return@withContext UserResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при удалении аватара", e)
            return@withContext UserResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}