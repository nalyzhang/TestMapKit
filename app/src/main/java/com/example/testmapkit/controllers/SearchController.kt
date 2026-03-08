package com.example.testmapkit.controllers

import com.example.testmapkit.EARTH_RADIUS
import com.example.testmapkit.models.LocationData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

class SearchController {

    fun getRandomNonZero(): Double {
        var random = 0.0
        do {
            random = Random.nextDouble(-1.0, 1.0)
        } while (random == 0.0)
        return random
    }

    fun searchRandomPosition(locationData: LocationData): LocationData{
        val randomLatitudeDistance = getRandomNonZero() * locationData.circleRadius / 10
        val randomLongitudeDistance = getRandomNonZero() * locationData.circleRadius / 10
        val deltaLatitude = (
                180 * randomLatitudeDistance
                ) / (
                PI * EARTH_RADIUS
                        )
        val cosLatitude = cos(locationData.latitude)
        val deltaLongitude = (
                180 * randomLongitudeDistance
                ) / (
                PI * EARTH_RADIUS * cosLatitude
                        )
        val randomLatitude = locationData.latitude + deltaLatitude
        val randomLongitude = locationData.longitude + deltaLongitude
        return LocationData(
            randomLongitude,
            randomLatitude,
            locationData.circleRadius
        )
    }
}