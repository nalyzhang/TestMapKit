package com.example.testmapkit.network

import com.example.testmapkit.dataModels.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== Аутентификация (djoser) ==========
    @POST("api/v1/auth/users/")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @POST("api/v1/auth/token/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthTokenResponse>

    @POST("api/v1/auth/token/logout/")
    suspend fun logout(): Response<Unit>

    @POST("api/v1/auth/users/set_password/")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @GET("api/v1/users/me/")
    suspend fun getCurrentUser(): Response<UserWithStatistic>

    @PUT("api/v1/users/me/")
    suspend fun putCurrentUser(@Body request: UserUpdate): Response<UserUpdate>

    // ========== Пользователи ==========
    @GET("api/v1/users/")
    suspend fun getUsers(): Response<PaginatedResponse<User>>

    @GET("api/v1/users/{id}/")
    suspend fun getUserById(
        @Path("id") userId: Int,
        @Query("statistic") includeStatistic: Boolean? = null
    ): Response<UserWithStatistic> // или UserWithStatistic если statistic=true

    // Друзья
    @POST("api/v1/users/{id}/friend/")
    suspend fun addFriend(@Path("id") friendId: Int): Response<UserWithStatistic>

    @DELETE("api/v1/users/{id}/friend/")
    suspend fun removeFriend(@Path("id") friendId: Int): Response<Unit>

    @GET("api/v1/users/friendslist/")
    suspend fun getFriendsList(): Response<PaginatedResponse<User>>

    // Статистика
    @GET("api/v1/users/{id}/statistic/")
    suspend fun getUserStatistic(
        @Path("id") userId: Int,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<UserStatistic>

    @GET("api/v1/users/me/statistic/")
    suspend fun getMyStatistic(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<UserStatistic>

    @GET("api/v1/users/statistics/")
    suspend fun getFriendsStatistics(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<FriendsStatisticsResponse>

    // Аватар
    @PUT("api/v1/users/me/avatar/")
    suspend fun updateAvatar(@Body request: AvatarUpdateRequest): Response<AvatarUpdateResponse>

    @DELETE("api/v1/users/me/avatar/")
    suspend fun deleteAvatar(): Response<Unit>

    // ========== Локации ==========
    @POST("api/v1/locations/")
    suspend fun createLocation(@Body location: LocationCreateRequest): Response<Location>

    // ========== Маршруты ==========
    @GET("api/v1/routes/")
    suspend fun getMyRoutes(
        @Query("address") address: String? = null
    ): Response<PaginatedResponse<Route>>

    @GET("api/v1/routes/{id}/")
    suspend fun getRouteById(@Path("id") routeId: Int): Response<Route>

    @POST("api/v1/routes/")
    suspend fun createRoute(@Body route: RouteCreateRequest): Response<Route>

    @DELETE("api/v1/routes/{id}/")
    suspend fun deleteRoute(@Path("id") routeId: Int): Response<Unit>
}