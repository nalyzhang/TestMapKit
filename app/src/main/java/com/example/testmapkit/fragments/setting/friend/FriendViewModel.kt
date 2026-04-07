package com.example.testmapkit.fragments.setting.friend

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapkit.TAG
import com.example.testmapkit.dataModels.User
import com.example.testmapkit.dataModels.UserWithStatistic
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.FriendRepository
import com.example.testmapkit.repositories.FriendResult
import kotlinx.coroutines.launch

class FriendViewModel (
    private val friendRepository: FriendRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    private val _friendsList = MutableLiveData<List<User>?>()
    val friendsList: LiveData<List<User>?> = _friendsList

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _userByID = MutableLiveData<UserWithStatistic?>()
    val userByID: LiveData<UserWithStatistic?> = _userByID

    private val _removeFriendState = MutableLiveData<FriendResult<Unit>?>()
    val removeFriendState: LiveData<FriendResult<Unit>?> = _removeFriendState

    private val _addFriend = MutableLiveData<FriendResult<UserWithStatistic>?>()
    val addFriend: LiveData<FriendResult<UserWithStatistic>?> = _addFriend


    /**
     * Получение списка друзей текущего пользователя (асинхронно)
     * Результат придет в LiveData currentUser
     */
    fun getFriendsList() {
        Log.d(TAG, "ViewModel: получение списка друзей")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить список друзей")
            _friendsList.value = null
            return
        }


        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = friendRepository.getFriendsList()
                Log.d(TAG, "Результат получения списка друзей: $result")
                _isLoading.value = false

                when (result) {
                    is FriendResult.Success -> {
                        _friendsList.value = result.data
                        Log.d(TAG, "Размер списка друзей: ${result.data.size}")
                    }
                    is FriendResult.Error -> {
                        Log.e(TAG, "Ошибка получения списка друзей: ${result.message}")
                        _errorMessage.value = result.message
                        _friendsList.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении списка друзей", e)
                _errorMessage.value = e.message ?: "Ошибка получения списка друзей"
                _friendsList.value = null
                _isLoading.value = false
            }
        }
    }

    /**
     * Получение профиля пользователя по ID
     */
    fun getUserByID(
        userID: Int
    ) {
        Log.d(TAG, "ViewModel: получение пользователя $userID")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно получить пользователя $userID")
            _userByID.value = null
            return
        }

        viewModelScope.launch {
            try {
                val result = friendRepository.getUserById(userID)
                Log.d(TAG, "Результат получения пользователя $userID: $result")

                when (result) {
                    is FriendResult.Success -> {
                        _userByID.value = result.data
                        Log.d(TAG, "Пользователь $userID: ${result.data.username}")
                    }
                    is FriendResult.Error -> {
                        Log.e(
                            TAG,
                            "Ошибка получения пользователя $userID: ${result.message}"
                        )
                        _errorMessage.value = result.message
                        _userByID.value = null
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при получении пользователя $userID", e)
                _errorMessage.value = e.message ?: "Ошибка получения данных пользователя $userID"
                _userByID.value = null
            }
        }
    }

    /**
     * Удаление друга
     */
    fun removeFriend(
        userID: Int
    ) {
        Log.d(TAG, "ViewModel: удаление друга")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно удалить друга $userID")
            _userByID.value = null
            return
        }

        _removeFriendState.value = FriendResult.Loading

        viewModelScope.launch {
            try {
                val result = friendRepository.removeFriend(userID)
                Log.d(TAG, "Результат удаления друга: $result")

                when (result) {
                    is FriendResult.Success -> {
                        _removeFriendState.value = FriendResult.Success(Unit)
                        Log.d(TAG, "Друг успешно удален")
                        _errorMessage.value = null
                    }
                    is FriendResult.Error -> {
                        _removeFriendState.value = FriendResult.Error(result.message, result.code)
                        Log.e(TAG, "Ошибка удаления друга: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    is FriendResult.Loading -> {
                        _removeFriendState.value = FriendResult.Loading
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при удалении друга", e)
                _removeFriendState.value = FriendResult.Error(
                    e.message ?: "Ошибка удаления друга")
            }
        }
    }

    /**
     * Добавление друга
     */
    fun addFriend(
        userID: Int
    ) {
        Log.d(TAG, "ViewModel: добавление друга")

        // Проверяем, есть ли токен
        if (!tokenManager.hasToken()) {
            Log.w(TAG, "Нет токена, невозможно добавить пользователя $userID")
            _userByID.value = null
            return
        }
        _isLoading.value = true
        _addFriend.value = FriendResult.Loading

        viewModelScope.launch {
            try {
                val result = friendRepository.addFriend(userID)
                Log.d(TAG, "Результат добавления друга: $result")
                _isLoading.value = false
                _addFriend.value = result

                when (result) {
                    is FriendResult.Success -> {
                        Log.d(TAG, "Друг ${result.data.username} успешно добавлен")
                        _errorMessage.value = null
                    }
                    is FriendResult.Error -> {
                        Log.e(TAG, "Ошибка добавления друга: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Исключение при добавлении друга", e)
                _addFriend.value = FriendResult.Error(e.message ?: "Ошибка добавления друга")
                _errorMessage.value = e.message ?: "Ошибка добавления друга"
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