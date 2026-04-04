package com.example.testmapkit.fragments.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.User
import com.example.testmapkit.dataModels.UserWithStatistic
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository
import com.example.testmapkit.repositories.UserResult
import kotlinx.coroutines.launch

class UserViewModel (
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    // Состояние регистрации
    private val _registrationState = MutableLiveData<UserResult<User>?>()
    val registrationState: LiveData<UserResult<User>?> = _registrationState

    // Состояние входа
    private val _loginState = MutableLiveData<UserResult<String>?>()
    val loginState: LiveData<UserResult<String>?> = _loginState

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Текущий пользователь
    private val _currentUser = MutableLiveData<UserWithStatistic?>()
    val currentUser: LiveData<UserWithStatistic?> = _currentUser

    // Флаг авторизации
    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _logoutState = MutableLiveData<UserResult<Unit>?>()
    val logoutState: LiveData<UserResult<Unit>?> = _logoutState

    private val _changePassword = MutableLiveData<UserResult<Unit>?>()
    val changePassword: LiveData<UserResult<Unit>?> = _changePassword

    init {
        Log.d(TAG, "UserViewModel инициализирован")
        checkAuthenticationStatus()
    }

    /**
     * Регистрация нового пользователя
     */
    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        Log.d(TAG, "ViewModel: запуск регистрации для $username")

        // Валидация данных
        val validationError = validateRegistrationData(
            username, email, password, firstName, lastName
        )

        if (validationError != null) {
            Log.e(TAG, "Ошибка валидации: $validationError")
            _errorMessage.value = validationError
            return
        }

        _isLoading.value = true
        _registrationState.value = UserResult.Loading

        viewModelScope.launch {
            val result = userRepository.register(
                username, email, password, firstName, lastName)
            Log.d(TAG, "Результат регистрации: $result")

            _registrationState.value = result
            _isLoading.value = false

            when (result) {
                is UserResult.Success -> {
                    Log.d(TAG,
                        "Регистрация успешна, пользователь: ${result.data.username}")
                    _errorMessage.value = null
                    // После успешной регистрации автоматически входим
                    login(email, password)
                }
                is UserResult.Error -> {
                    Log.e(TAG, "Ошибка регистрации: ${result.message}")
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Вход в систему
     */
    fun login(email: String, password: String) {
        Log.d(TAG, "ViewModel: запуск входа для $email")

        // Валидация
        val validationError = validateLoginData(email, password)
        if (validationError != null) {
            Log.e(TAG, "Ошибка валидации: $validationError")
            _errorMessage.value = validationError
            return
        }

        _isLoading.value = true
        _loginState.value = UserResult.Loading

        viewModelScope.launch {
            val result = userRepository.login(email, password)
            Log.d(TAG, "Результат входа: $result")

            _loginState.value = result
            _isLoading.value = false

            when (result) {
                is UserResult.Success -> {
                    Log.d(TAG, "Вход успешен, сохраняем токен")
                    _errorMessage.value = null
                    tokenManager.saveToken(result.data)
                    _isAuthenticated.value = true
                }
                is UserResult.Error -> {
                    Log.e(TAG, "Ошибка входа: ${result.message}")
                    _errorMessage.value = result.message
                    _isAuthenticated.value = false
                }
                else -> {}
            }
        }
    }

    /**
     * Получение текущего пользователя (асинхронно)
     * Результат придет в LiveData currentUser
     */
    fun getCurrentUser() {
        Log.d(TAG, "ViewModel: получение текущего пользователя")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить пользователя")
            _currentUser.value = null
            return
        }

        viewModelScope.launch {
            try {
                val result = userRepository.getCurrentUser()
                Log.d(TAG, "Результат получения текущего пользователя: $result")

                when (result) {
                    is UserResult.Success -> {
                        _currentUser.value = result.data
                        Log.d(TAG, "Текущий пользователь: ${result.data.username}")
                    }
                    is UserResult.Error -> {
                        Log.e(TAG, "Ошибка получения пользователя: ${result.message}")
                        _errorMessage.value = result.message
                        _currentUser.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении пользователя", e)
                _errorMessage.value = e.message ?: "Ошибка получения данных пользователя"
                _currentUser.value = null
            }
        }
    }

    /**
     * Выход из системы
     */
    fun logout() {
        Log.d(TAG, "ViewModel: выход из системы")

        _isLoading.value = true
        _logoutState.value = UserResult.Loading

        viewModelScope.launch {
            try {

                val result = userRepository.logout()
                Log.d(TAG, "Результат выхода: $result")

                _logoutState.value = result
                _isLoading.value = false

                when (result) {
                    is UserResult.Success -> {
                        Log.d(TAG, "Выход успешен")

                        // Очищаем токен
                        tokenManager.clearToken()

                        // Сбрасываем состояния
                        _currentUser.value = null
                        _isAuthenticated.value = false
                        _errorMessage.value = null

                        Log.d(TAG, "Пользователь вышел, данные очищены")
                    }
                    is UserResult.Error -> {
                        Log.e(TAG, "Ошибка выхода: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при выходе", e)
            }
        }
    }

    /**
    * Изменение пароля
     **/
    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        Log.d(TAG, "ViewModel: изменение пароля")

        val validationError = validateNewPassword(currentPassword, newPassword)
        if (validationError != null) {
            Log.e(TAG, "Ошибка валидации: $validationError")
            _errorMessage.value = validationError
            return
        }

        _isLoading.value = true
        _changePassword.value = UserResult.Loading

        viewModelScope.launch {
            try {

                val result = userRepository.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                Log.d(TAG, "Результат изменения пароля: $result")

                _changePassword.value = result
                _isLoading.value = false

                when (result) {
                    is UserResult.Success -> {
                        Log.d(TAG, "Успешное изменение пароля")

                        _errorMessage.value = null
                    }
                    is UserResult.Error -> {
                        Log.e(TAG, "Ошибка изменения пароля: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при изменении пароля", e)
            }
        }
    }

    /**
     * Проверка статуса авторизации
     */
    private fun checkAuthenticationStatus() {
        val hasToken = tokenManager.hasToken()
        Log.d(TAG, "Проверка авторизации: $hasToken")
        _isAuthenticated.value = hasToken

        // Если есть токен, пробуем получить данные пользователя
        if (hasToken) {
            getCurrentUser()
        }
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
        _registrationState.value = null
        _loginState.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }

    /**
     * Валидация данных регистрации
     */
    private fun validateRegistrationData(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): String? {
        return when {
            username.isBlank() -> "Введите username"
            username.length < 3 -> "Username должен быть не менее 3 символов"
            username.length > 150 -> "Username не должен превышать 150 символов"
            email.isBlank() -> "Введите email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(
                email).matches() -> "Введите корректный email"
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Пароль должен быть не менее 6 символов"
            firstName.isBlank() -> "Введите имя"
            firstName.length > 150 -> "Имя не должно превышать 150 символов"
            lastName.isBlank() -> "Введите фамилию"
            lastName.length > 150 -> "Фамилия не должна превышать 150 символов"
            else -> null
        }
    }

    /**
     * Валидация данных входа
     */
    private fun validateLoginData(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Введите email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(
                email).matches() -> "Введите корректный email"
            password.isBlank() -> "Введите пароль"
            else -> null
        }
    }

    /**
     * Валидация изменения пароля
     */
    private fun validateNewPassword(currentPassword: String, newPassword: String): String? {
        return when {
            currentPassword.isBlank() -> "Введите текущий пароль"
            newPassword.isBlank() -> "Введите новый пароль"
            currentPassword == newPassword -> "Пароль не должен повторяться"
            newPassword.length < 6 -> "Пароль должен быть не менее 6 символов"
            else -> null
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
}