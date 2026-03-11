package com.example.testmapkit.models

import com.yandex.mapkit.Time

class RouteData {

    private lateinit var startLocation: LocationData
    private lateinit var finalLocation: LocationData
    private var distance: Int = 0
    private lateinit var time: Time

    constructor(
        startLocationData: LocationData,
        finalLocationData: LocationData
        ) {
        startLocation = startLocationData
        finalLocation = finalLocationData
        // TODO: расчет расстояния
        // TODO: таймер
    }
}