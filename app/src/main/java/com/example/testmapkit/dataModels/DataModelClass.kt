package com.example.testmapkit.dataModels

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("avatar") val avatar: String? = null, // URL картинки
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_friend") val isFriend: Boolean = false
){
    fun getFullAvatarUrl(baseUrl: String = "http://192.168.0.202:8000"): String {
        if (avatarUrl.isNullOrEmpty()) return ""
        return if (avatarUrl.startsWith("http")) {
            avatarUrl
        } else {
            "$baseUrl$avatarUrl"
        }
    }
}

// Расширенный пользователь (со статистикой)
data class UserWithStatistic(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_friend") val isFriend: Boolean = false,
    @SerializedName("routes") val routes: List<Route> = emptyList(),
    @SerializedName("routes_count") val routesCount: Int = 0
) {
    fun getFullAvatarUrl(baseUrl: String = "http://192.168.0.202:8000"): String {
        if (avatarUrl.isNullOrEmpty()) return ""
        return if (avatarUrl.startsWith("http")) {
            avatarUrl
        } else {
            "$baseUrl$avatarUrl"
        }
    }
}

// Изменение пользователя
data class UserUpdate(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
)

// Location.kt
data class Location(
    @SerializedName("id") val id: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("radius") val radius: Double? = null,
    @SerializedName("address") val address: String,
    @SerializedName("time") val time: String // ISO 8601 формат
)

// Route.kt
data class Route(
    @SerializedName("id") val id: Int,
    @SerializedName("start") val start: Location,
    @SerializedName("finish") val finish: Location,
    @SerializedName("distance") val distance: Double,
    @SerializedName("time") val time: String, // Формат "HH:MM:SS"
    @SerializedName("date") val date: String  // Формат "YYYY-MM-DD"
)

// Request для создания маршрута (отдельный класс)
data class RouteCreateRequest(
    @SerializedName("start_id") val startId: Int,
    @SerializedName("finish_id") val finishId: Int,
    @SerializedName("distance") val distance: Double,
    @SerializedName("time") val time: String,
    @SerializedName("date") val date: String
)

// Статистика пользователя
data class UserStatistic(
    @SerializedName("user") val user: User,
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("routes_count") val routesCount: Int,
    @SerializedName("average_radius") val averageRadius: Double
)

// Статистика друзей (обертка)
data class FriendsStatisticsResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<FriendStatistic>
)

data class FriendStatistic(
    @SerializedName("user") val user: User,
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("routes_count") val routesCount: Int
)

// Request для установки аватара
data class AvatarUpdateRequest(
    @SerializedName("avatar") val avatarBase64: String
)

data class AvatarUpdateResponse(
    @SerializedName("avatar") val avatarUrl: String
)

// Пагинированные ответы
data class PaginatedResponse<T>(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<T>
)

// Request для регистрации
data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)

// Request для логина
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthTokenResponse(
    @SerializedName("auth_token") val authToken: String
)

// Request для смены пароля
data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)