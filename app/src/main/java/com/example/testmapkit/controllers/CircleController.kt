package com.example.testmapkit.controllers

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testmapkit.DEFAULT_RADIUS_KM
import com.example.testmapkit.R
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.Map

class CircleController(private val activity: AppCompatActivity) {
    private lateinit var circle: Circle
    private var radiusCircle: Int = DEFAULT_RADIUS_KM
    private var isEnable: Boolean = true

    fun clickCircle(location: Location, map: Map) {
        if (isEnable) getCircle(location, map)
    }

    private fun getCircle(location: Location, map: Map){
        circle = Circle(
            location.position,
            (radiusCircle * 100f) // в метрах
        )
        removeCircle(map)
        map.mapObjects.addCircle(circle).apply {
            strokeWidth = 1f
            strokeColor = ContextCompat.getColor(activity, R.color.brilliant_blue)
            fillColor = ContextCompat.getColor(activity, R.color.light_blue)
            zIndex = 0f
        }
    }

    private fun removeCircle(map: Map) {
        map.mapObjects.clear()
    }

    fun updateRadius(radius: Int, location: Location, map: Map) {
        radiusCircle = radius
        getCircle(location, map)
    }

    fun getCircleRadius(): Int {
        return radiusCircle
    }

    fun fixCircle() {
        isEnable = !isEnable
    }
}