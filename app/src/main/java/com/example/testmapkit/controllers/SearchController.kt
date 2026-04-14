package com.example.testmapkit.controllers

import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.example.testmapkit.EARTH_RADIUS
import com.example.testmapkit.MAIN
import com.example.testmapkit.models.LocationData
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

class SearchController {

    private lateinit var geocoder: Geocoder

    private fun getRandomNonZero(): Double {
        var random = 0.0
        do {
            random = Random.nextDouble(-1.0, 1.0)
        } while (random == 0.0)
        return random
    }

    private fun searchRandomPosition(longitude: Double,
                                     latitude: Double,
                                     circleRadius: Double): LocationData{
        val randomLatitudeDistance = getRandomNonZero() * circleRadius / 10
        val randomLongitudeDistance = getRandomNonZero() * circleRadius / 10
        val deltaLatitude = (
                180 * randomLatitudeDistance
                ) / (
                PI * EARTH_RADIUS
                        )
        val cosLatitude = cos(latitude)
        val deltaLongitude = (
                180 * randomLongitudeDistance
                ) / (
                PI * EARTH_RADIUS * cosLatitude
                        )
        val randomLatitude = latitude + deltaLatitude
        val randomLongitude = longitude + deltaLongitude
        return LocationData(
            randomLongitude,
            randomLatitude,
            circleRadius
        )
    }

    fun getAddress(longitude: Double,
                   latitude: Double): Address?{
        return geocoder.getFromLocation(
            latitude,
            longitude,
            1
        )?.get(0)
    }

    fun getRandomAddress(longitude: Double,
                   latitude: Double,
                   circleRadius: Double): LocationData{
        geocoder = Geocoder(MAIN, Locale.getDefault())

        val randomLocationData = searchRandomPosition(
            longitude,
            latitude,
            circleRadius)

        Log.d(
            "MainActivity",
            "latitude: ${randomLocationData.latitude} longitude: ${randomLocationData.longitude}"
        )

        val address = getAddress(
            randomLocationData.longitude,
            randomLocationData.latitude
        )

        randomLocationData.setAddress(address)

        return randomLocationData
    }

    // TODO: обработка на валидность адресов
}