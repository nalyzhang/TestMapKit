package com.example.testmapkit

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.testmapkit.databinding.ActivityMainBinding
import kotlin.experimental.ExperimentalObjCEnum


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    @SuppressLint("DefaultLocale", "UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            MAIN = this

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
}