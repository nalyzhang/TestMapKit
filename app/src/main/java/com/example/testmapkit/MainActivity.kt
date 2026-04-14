package com.example.testmapkit

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.testmapkit.databinding.ActivityMainBinding
import com.example.testmapkit.services.ChronometerService
import com.example.testmapkit.services.LocationService


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    private var locationService: LocationService? = null
    private var chronometerService: ChronometerService? = null

    private var isLocationServiceBound = false
    private var isChronometerServiceBound = false

    // Колбэки для фрагментов
    private var locationUpdateListener: LocationService.LocationUpdateListener? = null

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isLocationServiceBound = true

            // Устанавливаем слушатель если есть
            locationUpdateListener?.let {
                locationService?.addLocationListener(it)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            isLocationServiceBound = false
        }
    }

    private val chronometerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChronometerService.ChronometerBinder
            chronometerService = binder.getService()
            isChronometerServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            chronometerService = null
            isChronometerServiceBound = false
        }
    }

    @SuppressLint("DefaultLocale", "UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            MAIN = this
            startServices()

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController

            binding.navBottom.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.location_item -> {
                        if (PROCESSING) navController.navigate(R.id.walkFragment)
                        else navController.navigate(R.id.locationFragment)
                        true
                    }

                    R.id.history_item -> {
                        navController.navigate(R.id.historyFragment)
                        true
                    }

                    R.id.profile_item -> {
                        navController.navigate(R.id.settingFragment)
                        true
                    }

                    R.id.statistic_item -> {
                        navController.navigate(R.id.statisticFragment)
                        true
                    }

                    else -> false
                }
            }
        } catch (e: Exception){
            Log.e("MainActivityFragments", "$e")
        }
    }

    private fun startServices() {
        // Запуск LocationService
        val locationIntent = Intent(this, LocationService::class.java)
        startService(locationIntent)
        bindService(locationIntent, locationServiceConnection, BIND_AUTO_CREATE)

        // Запуск ChronometerService
        val chronometerIntent = Intent(this, ChronometerService::class.java)
        startService(chronometerIntent)
        bindService(
            chronometerIntent, chronometerServiceConnection, BIND_AUTO_CREATE)
    }

    fun getLocationService(): LocationService? = locationService

    fun getChronometerService(): ChronometerService? = chronometerService

    fun setLocationUpdateListener(listener: LocationService.LocationUpdateListener?) {
        this.locationUpdateListener = listener
        // Если сервис уже подключен, добавляем слушатель
        if (isLocationServiceBound) {
            listener?.let {
                locationService?.addLocationListener(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isLocationServiceBound) {
            unbindService(locationServiceConnection)
        }
        if (isChronometerServiceBound) {
            unbindService(chronometerServiceConnection)
        }
    }
}