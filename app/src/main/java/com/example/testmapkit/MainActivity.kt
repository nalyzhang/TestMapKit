package com.example.testmapkit

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.testmapkit.controllers.LocationController
import com.yandex.mapkit.MapKitFactory
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.testmapkit.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var location: LocationController
    lateinit var navController: NavController

    @SuppressLint("DefaultLocale", "UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.key)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MapKitFactory.initialize(this)

        MAIN = this

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

//    override fun onStart() {
//        MapKitFactory.getInstance().onStart()
//        mapView.onStart()
//        location.startLocationUpdates()
//        super.onStart()
//    }
//
//    override fun onStop() {
//        mapView.onStop()
//        location.startLocationUpdates()
//        MapKitFactory.getInstance().onStop()
//        super.onStop()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Возобновляем обновления, когда активность снова видна
//        location.startLocationUpdates()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        // Приостанавливаем обновления, чтобы сэкономить батарею
//        location.stopLocationUpdates()
//    }
}