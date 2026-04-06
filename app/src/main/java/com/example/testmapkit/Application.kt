package com.example.testmapkit

import android.app.Application
import android.util.Log
import com.yandex.mapkit.MapKitFactory

class Application : Application() {

    companion object {
        private lateinit var instance: Application

        fun getInstance(): Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            MapKitFactory.setApiKey(BuildConfig.key)
            MapKitFactory.initialize(this)
            Log.d(TAG, "MapKit initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MapKit: ${e.message}", e)
        }
    }
}