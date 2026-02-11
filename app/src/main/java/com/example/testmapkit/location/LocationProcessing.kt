package com.example.testmapkit.location

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

class LocationProcessing(private val activity: AppCompatActivity, private val mapView: MapView) {


    private lateinit var locationManager: LocationManager
    private var lastKnownLocation: Location? = null
    private val map = mapView.mapWindow.map
    // Добавляем поле для сохранения ссылки на слушатель, чтобы он не был собран сборщиком мусора
    private var locationListener: LocationListener? = null
    // Добавляем поле для хранения слоя местоположения пользователя
    private var userLocationLayer: UserLocationLayer? = null
    private var circle = CircleProcessing(activity)

    // Проверка на наличие разрешения на использование локации
    private fun checkLocationPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    // Запрос на разрешение использования геолокации
    private fun requestLocationPermission(){
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )
    }

    // Включение локационных сервисов
    fun enableLocationServices() {
        if (checkLocationPermissions()) {
            setupLocationServices()
        } else {
            requestLocationPermission()
        }
    }

    // Настройка локационных сервисов
    private fun setupLocationServices() {

        try {
            // 1. Создаем слой отображения местоположения пользователя
            val mapKit: MapKit = MapKitFactory.getInstance()
            requestLocationPermission()
            val locationMapkit = mapKit.createUserLocationLayer(mapView.mapWindow)
            locationMapkit.isVisible = true

            // 2. Создаем LocationManager для отслеживания местоположения
            locationManager = MapKitFactory.getInstance().createLocationManager()

            // 3. Создаем и сохраняем слушатель в поле класса, чтобы он не был удален GC
            locationListener = object : LocationListener {
                override fun onLocationUpdated(location: Location) {
                    // Сохраняем последнее местоположение
                    lastKnownLocation = location

                    // Добавляем Toast для отладки - видим, когда приходит новая локация
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "Обновлена локация: ${location.position.latitude}, ${location.position.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                        moveToLocation(location)
                    }
                }

                // Добавляем обработку статуса в UI-потоке
                override fun onLocationStatusUpdated(status: LocationStatus) {
                    activity.runOnUiThread {
                        when (status) {
                            LocationStatus.NOT_AVAILABLE -> {
                                Toast.makeText(
                                    activity,
                                    "Служба местоположения недоступна",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            LocationStatus.AVAILABLE -> {
                                // Служба доступна
                            }

                            else -> {
                                // Другие статусы
                            }
                        }
                    }
                }
            }

            // 4. Подписываемся на обновления

            locationListener?.let { listener ->
                // Создаем настройки подписки для более тонкого контроля
                val subscriptionSettings = SubscriptionSettings().apply {
                    UseInBackground.ALLOW
                    Purpose.GENERAL
                }
                locationManager.subscribeForLocationUpdates(
                    subscriptionSettings,
                    listener
                )
            }

        } catch (e: Exception){
            // Обрабатываем исключения и показываем пользователю сообщение об ошибке
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "Ошибка инициализации геолокации: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Добавляем метод для ручного запроса обновления местоположения
    fun requestLocationUpdate() {
        locationManager.requestSingleUpdate(
            object : LocationListener {
                override fun onLocationUpdated(location: Location) {
                    lastKnownLocation = location
                    // Обновляем UI в UI-потоке
                    activity.runOnUiThread {
                        moveToLocation(location)
                    }
                }

                override fun onLocationStatusUpdated(status: LocationStatus) {
                    // Обработка статуса (можно оставить пустым)
                }
            }
        )
    }

    // Функция для перемещения позиции камеры на точку пользователя
    fun moveToUserLocation() {
        requestLocationUpdate()
        lastKnownLocation?.let { location ->
            moveToLocation(location)
        } ?: run {
        }
    }

    // Для картинки перемещения
    private fun moveToLocation(location: Location) {
        val point = location.position

        // Проверяем, что координаты валидны
        if (point.latitude == 0.0 && point.longitude == 0.0) {
            return
        }

        // Перемещаем камеру на местоположение пользователя
        map.move(
            CameraPosition(
                point,         // Координаты пользователя
                17.0f,         // Уровень приближения
                0.0f,          // Азимут (направление камеры)
                0.0f           // Наклон камеры
            ),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun getLocation(location: Location) {
        Toast.makeText(
            activity,
            "Текущая локация: ${location.position.longitude} ${location.position.latitude}",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun getTextLocation() {
        lastKnownLocation?.let {
            circle.clickCircle(it, map)
            getLocation(it)
        } ?: run {
            Toast.makeText(
                activity,
                "Местоположение еще не определено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun changeCircleRadius(radius: Int) {
        lastKnownLocation?.let {
            circle.updateRadius(radius, it, map)
            getLocation(it)
        } ?: run {
            Toast.makeText(
                activity,
                "Местоположение еще не определено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Добавляем методы для управления жизненным циклом

    // Метод для возобновления обновлений локации (вызывать в onStart)
    fun startLocationUpdates() {
        locationListener?.let { listener ->
            try {
                val subscriptionSettings = SubscriptionSettings().apply {
                    UseInBackground.ALLOW
                    Purpose.GENERAL
                }
                // Подписываемся заново на случай, если подписка была отменена
                locationManager.subscribeForLocationUpdates(subscriptionSettings, listener)
            } catch (e: Exception) {
                // Игнорируем исключение, если уже подписаны
            }
        }
    }

    // Метод для остановки обновлений локации (вызывать в onStop)
    fun stopLocationUpdates() {
        locationListener?.let { listener ->
            // Отписываемся от обновлений для экономии батареи
            locationManager.unsubscribe(listener)
        }
    }

    // Метод для очистки ресурсов (вызывать в onDestroy)
    fun cleanup() {
        stopLocationUpdates()
        locationListener = null  // Освобождаем ссылку на слушатель
        userLocationLayer = null // Освобождаем ссылку на слой
    }
}