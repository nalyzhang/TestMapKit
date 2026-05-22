package com.example.testmapkit.controllers

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testmapkit.DEFAULT_RADIUS_KM
import com.example.testmapkit.R
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.Map

class CircleController(private val activity: AppCompatActivity) {
    private var radiusCircle: Int = DEFAULT_RADIUS_KM
    private var isEnable: Boolean = true

    private var circleMapObject: CircleMapObject? = null

    fun drawCircle(location: Location, map: Map){
        val circle = Circle(
            location.position,
            (radiusCircle * 100f) // в метрах
        )
        if (circleMapObject == null) {
            // Создаем круг в первый раз
            circleMapObject = map.mapObjects.addCircle(circle).apply {
                strokeWidth = 3f
                strokeColor = ContextCompat.getColor(activity, R.color.brilliant_blue)
                fillColor = ContextCompat.getColor(activity, R.color.light_blue)
                zIndex = 0f
            }
            Log.d("LocationServer","Круг создан впервые")
        } else {
            // Обновляем существующий круг - БЕЗ ПЕРЕСОЗДАНИЯ!
            circleMapObject?.apply {
                geometry = circle  // Меняем геометрию (позицию и радиус)
                // strokeWidth и цвета остаются теми же
                Log.d("LocationServer","Круг обновлен: радиус=${radiusCircle*100}м, позиция=${location.position.latitude},${location.position.longitude}")
            }
        }
    }

    fun updateRadius(radius: Int, location: Location, map: Map) {
        val oldRadius = radiusCircle
        radiusCircle = radius

        // Обновляем круг только если радиус действительно изменился
        if (oldRadius != radiusCircle) {
            drawCircle(location, map)
            Log.d("LocationServer","Радиус изменен: $oldRadius -> $radiusCircle")
        }
    }

    fun fixCircle() {
        isEnable = !isEnable
    }
}