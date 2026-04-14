package com.example.testmapkit.controllers

import android.util.Log
import com.example.testmapkit.EARTH_RADIUS
import com.example.testmapkit.TAG
import com.example.testmapkit.models.LocationData
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

class SearchController {

    private var randomAddress: String? = null


    private fun getRandomNonZero(): Double {
        var random: Double
        do {
            random = Random.nextDouble(-1.0, 1.0)
        } while (random == 0.0)
        return random
    }

    private fun searchRandomPosition(longitude: Double,
                                     latitude: Double,
                                     circleRadius: Double): LocationData{
        val randomLatitudeDistance = getRandomNonZero() * circleRadius
        val randomLongitudeDistance = getRandomNonZero() * circleRadius
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

    suspend fun getCurrentAddress(
        point: Point,
        radius: Double,
        maxAttempts: Int = 10
    ): LocationData? {
        repeat(maxAttempts) { attempt ->

            Log.d(
                "MainActivity",
                "Попытка ${attempt + 1}: latitude: ${point.latitude} longitude: ${point.longitude}"
            )

            val address = getOrganisationAddress(
                point
            )

            Log.d(TAG, "Получен адрес: $address")

            if (!address.isNullOrEmpty() && validateAddress(address)) {
                val location = LocationData(
                    point.longitude,
                    point.latitude,
                    radius
                )
                location.setAddress(address)
                return location
            }
        }

        Log.d(TAG, "Не удалось получить валидный адрес после $maxAttempts попыток")
        return null
    }

    suspend fun getRandomAddress(
        point: Point,
        circleRadius: Double,
        maxAttempts: Int = 10
    ): LocationData? {
        repeat(maxAttempts) { attempt ->

            val randomLocationData = searchRandomPosition(
                point.longitude,
                point.latitude,
                circleRadius
            )

            Log.d(
                "MainActivity",
                "Попытка ${attempt + 1}: latitude: ${randomLocationData.latitude} longitude: ${randomLocationData.longitude}"
            )


            val point = Point(randomLocationData.latitude, randomLocationData.longitude)

            val address = getOrganisationAddress(
                point
            )

            Log.d(TAG, "Получен адрес: $address")

            if (!address.isNullOrEmpty() && validateAddress(address)) {
                randomLocationData.setAddress(address)
                return randomLocationData
            }
        }

        Log.d(TAG, "Не удалось получить валидный адрес после $maxAttempts попыток")
        return null
    }

    private suspend fun getOrganisationAddress(
        point: Point) : String? = suspendCancellableCoroutine { continuation ->

        val searchManager = SearchFactory.getInstance().createSearchManager(
            SearchManagerType.COMBINED)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.BIZ.value
            resultPageSize = 64
            geometry = true
        }


        searchManager.submit(point, 16, searchOptions,
            object: Session.SearchListener {
                override fun onSearchError(error: com.yandex.runtime.Error) {
                    Log.d(TAG, "Error $error")
                    continuation.resume(null)
                }
                override fun onSearchResponse(response: Response) {
                    val geoObject = response.collection.children.firstOrNull()?.obj
                        ?.metadataContainer
                        ?.getItem(BusinessObjectMetadata::class.java)
                    val randomAddress = geoObject
                        ?.address
                        ?.formattedAddress
                        .toString()
                    val organization = geoObject
                        ?.name
                    Log.d(TAG, "Success $randomAddress, $organization")
                    continuation.resume(randomAddress)
                }
            })
    }

    private fun validateAddress(randomAddress: String) : Boolean {
        return randomAddress.any { it.isDigit() }
    }
}