package com.example.testmapkit.location

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class Location(private val activity: AppCompatActivity, private val mapView: MapView) {


    private lateinit var locationManager: LocationManager
    private var lastKnownLocation: com.yandex.mapkit.location.Location? = null
    private var isFirstLocation = true

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
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
            0)
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

        val mapKit: MapKit = MapKitFactory.getInstance()
        requestLocationPermission()
        val locationMapkit = mapKit.createUserLocationLayer(mapView.mapWindow)
        locationMapkit.isVisible = true
        locationMapkit.isHeadingModeActive = true

        locationManager = MapKitFactory.getInstance().createLocationManager() // Получаем LocationManager для отслеживания местоположения

        // Настраиваем слушатель обновлений местоположения
        val locationListener = object : LocationListener {
            override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                // Сохраняем последнее местоположение
                lastKnownLocation = location

                // Автоматически перемещаемся к пользователю при первом получении местоположения
                if (isFirstLocation) {
                    moveToLocation(location)
                    isFirstLocation = false
                }
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {
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

        // Подписываемся на обновления местоположения
        locationManager.subscribeForLocationUpdates(
            SubscriptionSettings(UseInBackground.ALLOW, Purpose.GENERAL),
            locationListener
        )
    }

    // Функция для перемещения позиции камеры на точку пользователя
    fun moveToUserLocation() {
        lastKnownLocation?.let {
            moveToLocation(it)
        } ?: run {
            Toast.makeText(
                activity,
                "Местоположение еще не определено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Для картинки перемещения
    private fun moveToLocation(location: com.yandex.mapkit.location.Location) {
        val point = location.position

        // Проверяем, что координаты валидны
        if (point.latitude == 0.0 && point.longitude == 0.0) {
            return
        }

        val map = mapView.mapWindow.map

        // Перемещаем камеру на местоположение пользователя
        map.move(
            CameraPosition(
                point,         // Координаты пользователя
                17.0f,         // Уровень приближения
                0.0f,          // Азимут (направление камеры)
                0.0f           // Наклон камеры
            ),
            com.yandex.mapkit.Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    // Начальная точка (если геолокации нет - дворцовая площадь)
    fun setInitialMapPosition() {
        val map = mapView.mapWindow.map
        map.move(
            CameraPosition(
                Point(59.939016, 30.314434),
                17.0f,
                150.0f,
                30.0f
            )
        )
    }
}