package com.example.testmapkit.location

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testmapkit.AppConstants
import com.example.testmapkit.R
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.Map

class CircleProcessing(private val activity: AppCompatActivity) {

    private var constants = AppConstants()
    private lateinit var circle: Circle
    private var isEnabled = false
    private var radiusCircle: Int = constants.DEFAULT_RADIUS_KM

    fun clickCircle(location: Location, map: Map) {
        if (isEnabled) {
            removeCircle(map)
            isEnabled = false
        } else {
            getCircle(location, map)
            isEnabled = true
        }
    }

    private fun getCircle(location: Location, map: Map){
        map.mapObjects.clear()
        circle = Circle(
            location.position,
            (radiusCircle * 100f) // в метрах
        )
        map.mapObjects.addCircle(circle).apply {
            strokeWidth = 1f
            strokeColor = ContextCompat.getColor(activity, R.color.brilliant_blue)
            fillColor = ContextCompat.getColor(activity, R.color.light_blue)
        }
    }

    private fun removeCircle(map: Map) {
        map.mapObjects.clear()
    }

    fun updateRadius(radius: Int, location: Location, map: Map) {
        radiusCircle = radius
        getCircle(location, map)
    }

}