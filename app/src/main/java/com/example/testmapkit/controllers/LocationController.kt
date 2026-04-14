package com.example.testmapkit.controllers

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground
class LocationController(private val context: Context) {


    private lateinit var locationManager: LocationManager
    private var lastKnownLocation: Location? = null
    // Добавляем поле для сохранения ссылки на слушателя, чтобы он не был собран сборщиком мусора
    private var locationListener: LocationListener? = null

    // Слушатели для обновлений
    private var listeners = mutableListOf<LocationUpdateListener>()

    interface LocationUpdateListener {
        fun onLocationUpdated(location: Location)
        fun onLocationStatusChanged(status: LocationStatus)
    }

    // Проверка на наличие разрешения на использование локации
    private fun checkLocationPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    // Инициализация и запуск
    fun startLocationTracking() {
        if (checkLocationPermissions()) {
            setupLocationServices()
        } else {
            Log.e("LocationServer", "Нет разрешений на геолокацию")
            // В сервисе нельзя запросить разрешения, они должны быть получены заранее
        }
    }
    private fun setupLocationServices() {

        try {
            // Создаем LocationManager
            locationManager = MapKitFactory.getInstance().createLocationManager()
            Log.d("LocationService", "Manager is created")

            // Создаем и сохраняем слушатель в поле класса, чтобы он не был удален GC
            locationListener = object : LocationListener {
                override fun onLocationUpdated(location: Location) {
                    // Сохраняем последнее местоположение
                    lastKnownLocation = location

                    // Добавляем Toast для отладки - видим, когда приходит новая локация
                    listeners.forEach { it.onLocationUpdated(location) }

                    Log.d(
                        "LocationService",
                        "Обновлена локация: ${location.position.latitude}, ${location.position.longitude}"
                    )
                }

                // Добавляем обработку статуса в UI-потоке
                override fun onLocationStatusUpdated(status: LocationStatus) {
                    listeners.forEach { it.onLocationStatusChanged(status) }

                    when (status) {
                        LocationStatus.NOT_AVAILABLE -> {
                            Log.e("LocationService", "Служба местоположения недоступна")
                        }
                        LocationStatus.AVAILABLE -> {
                            Log.d("LocationService", "Служба местоположения доступна")
                        }
                        else -> {}
                        }
                    }
                }
            Log.d("LocationService", "Location listener is created")

            // Подписываемся на обновления

            locationListener?.let { listener ->
                // Создаем настройки подписки для более тонкого контроля
                val subscriptionSettings = SubscriptionSettings(
                    UseInBackground.ALLOW,
                    Purpose.GENERAL
                )
                Log.d("LocationService", "Настройки сервиса подписки есть")
                locationManager.subscribeForLocationUpdates(
                    subscriptionSettings,
                    listener
                )

                Log.d("LocationService", "Подписка на обновления локации оформлена")
            }

        } catch (e: Exception){
            // Обрабатываем исключения и показываем пользователю сообщение об ошибке
            Log.e("LocationService", "Ошибка инициализации: ${e.message}")
        }
    }

    fun stopLocationTracking() {
        locationListener?.let { listener ->
            locationManager.unsubscribe(listener)
        }
    }

    fun getCurrentLocation(): Location? = lastKnownLocation

    fun addListener(listener: LocationUpdateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: LocationUpdateListener) {
        listeners.remove(listener)
    }

    // Метод для очистки ресурсов (вызывать в onDestroy)
    fun cleanup() {
        stopLocationTracking()
        locationListener = null
        listeners.clear()
    }
}