package com.example.testmapkit

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.testmapkit.controllers.LocationController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import androidx.core.graphics.createBitmap

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locationButton: Button
    private lateinit var startButton: Button
    private lateinit var location: LocationController
    private lateinit var radiusCircleText: TextView
    private lateinit var radiusCircleBar: SeekBar

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.key)
        MapKitFactory.initialize(this@MainActivity)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map_view)

        locationButton = findViewById(R.id.location_btn)
        startButton = findViewById(R.id.start_btn)
        radiusCircleText = findViewById(R.id.radiusSize_text)
        radiusCircleBar = findViewById(R.id.radiusSize_bar)

        radiusCircleBar.min = AppConstants().MIN_RADIUS_KM
        radiusCircleBar.max = AppConstants().MAX_RADIUS_KM
        radiusCircleBar.progress = AppConstants().DEFAULT_RADIUS_KM

        radiusCircleText.text = String.format(
            AppConstants().RADIUS_TEXT,
            radiusCircleBar.progress * AppConstants().RADIUS_SCALE_FACTOR
        )

        location = LocationController(this, mapView)

        location.enableLocationServices()

        locationButton.setOnClickListener {
            location.moveToUserLocation()
        }

        startButton.setOnClickListener {
            val randomLocation = location.getTextLocation()
            if (randomLocation != null) {
                val randomPoint = Point(randomLocation.latitude, randomLocation.longitude)
                mapView.mapWindow.map.move(
                    CameraPosition(randomPoint, 15.0f, 0.0f, 0.0f)
                )
                val placemark = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
                    geometry = randomPoint
                    val drawable = AppCompatResources.getDrawable(this@MainActivity, R.drawable.baseline_location_on_24)
                    if (drawable != null) {
                        // Конвертируем Drawable в Bitmap
                        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)

                        setIcon(ImageProvider.fromBitmap(bitmap))
                    }
                    zIndex = 100f
                }
            } else {
                // Обрабатываем исключения и показываем пользователю сообщение об ошибке
                this.runOnUiThread {
                    Toast.makeText(
                        this,
                        "Ошибка получения координат",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        radiusCircleBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
            override fun onProgressChanged(radiusCircleBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusCircleText.text = String.format(
                    AppConstants().RADIUS_TEXT,
                    progress * AppConstants().RADIUS_SCALE_FACTOR
                )
                location.changeCircleRadius(progress)
            }

            override fun onStartTrackingTouch(radiusCircleBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(radiusCircleBar: SeekBar?) {
            }
        })
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        location.startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        mapView.onStop()
        location.startLocationUpdates()
    }
}