package com.example.testmapkit

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.testmapkit.location.LocationProcessing
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

class MainActivity : AppCompatActivity() {

    private var constants = AppConstants()
    private lateinit var mapView: MapView

    private lateinit var locationButton: Button

    private lateinit var startButton: Button

    private lateinit var location: LocationProcessing

    private lateinit var radiusCircleText: TextView

    private lateinit var radiusCircleBar: SeekBar

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

        radiusCircleBar.min = constants.MIN_RADIUS_KM
        radiusCircleBar.max = constants.MAX_RADIUS_KM
        radiusCircleBar.progress = constants.DEFAULT_RADIUS_KM

        radiusCircleText.text = String.format(constants.RADIUS_TEXT, radiusCircleBar.progress * constants.RADIUS_SCALE_FACTOR)

        location = LocationProcessing(this, mapView)

        location.enableLocationServices()

        locationButton.setOnClickListener {
            location.moveToUserLocation()
        }

        startButton.setOnClickListener {
            location.getTextLocation()
        }

        radiusCircleBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
            override fun onProgressChanged(radiusCircleBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusCircleText.text = String.format(
                    constants.RADIUS_TEXT, progress * constants.RADIUS_SCALE_FACTOR)
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