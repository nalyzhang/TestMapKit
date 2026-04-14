package com.example.testmapkit.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testmapkit.R
import com.example.testmapkit.controllers.LocationController
import com.example.testmapkit.models.LocationData
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationStatus

class LocationService : Service() {

    private val binder = LocationBinder()
    private lateinit var locationController: LocationController

    // Текущая позиция
    private var currentLocation: Location? = null

    // Слушатели из фрагментов
    private val fragmentListeners = mutableListOf<LocationUpdateListener>()

    // Флаг для проверки достижения цели
    var isGoalReached = false
        private set

    // TODO: Добавить поля для маршрута
    // val routePoints = mutableListOf<Point>()

    interface LocationUpdateListener {
        fun onLocationUpdated(location: Location)
        fun onGoalReached() // TODO: Реализовать позже
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализируем контроллер
        locationController = LocationController(applicationContext)

        // Подписываемся на обновления от контроллера
        locationController.addListener(object :
            LocationController.LocationUpdateListener {
            override fun onLocationUpdated(location: Location) {
                currentLocation = location
                fragmentListeners.forEach { it.onLocationUpdated(location) }
            }

            override fun onLocationStatusChanged(status: LocationStatus) {
                when (status) {
                    LocationStatus.AVAILABLE -> {
                        Log.d("LocationService", "Location service available")
                    }
                    LocationStatus.NOT_AVAILABLE -> {
                        Log.e("LocationService", "Location service not available")
                    }
                    else -> {}
                }
            }
        })

        // Запускаем foreground сервис
        startForegroundService()

        // Запускаем отслеживание
        locationController.startLocationTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Обработка команд из фрагментов
        when (intent?.action) {
            "START_QUEST" -> {
                val radius = intent.getIntExtra("radius", 1)
                startQuest(radius)
            }
            "STOP_QUEST" -> {
                stopQuest()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "location_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Служба местоположения",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отслеживает ваше местоположение во время квеста"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Поиск маршрута")
            .setContentText("Идет отслеживание местоположения")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startQuest(radius: Int) {
        // TODO: Генерация случайного адреса через locationController.getRandomAddress(radius)
        // TODO: Сохранение целевой точки
        Log.d("LocationService", "Квест начат с радиусом $radius км")
    }

    private fun stopQuest() {
        // TODO: Остановка квеста, сохранение результатов
        Log.d("LocationService", "Квест остановлен")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // locationController.cleanup()
    }

    // Методы для взаимодействия с фрагментами
    fun addLocationListener(listener: LocationUpdateListener) {
        if (!fragmentListeners.contains(listener)) {
            fragmentListeners.add(listener)
        }
    }

    fun removeLocationListener(listener: LocationUpdateListener) {
        fragmentListeners.remove(listener)
    }

    fun getCurrentLocation(): Location? = currentLocation

    fun getRandomAddress(radius: Double): LocationData? =
        locationController.getRandomAddress(radius)
}