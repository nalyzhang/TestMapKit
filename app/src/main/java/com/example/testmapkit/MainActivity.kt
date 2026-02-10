package com.example.testmapkit

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.testmapkit.location.LocationProcessing
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private lateinit var locationButton: Button

    private lateinit var startButton: Button

    private lateinit var location: LocationProcessing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.key)
        MapKitFactory.initialize(this@MainActivity)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map_view)

        locationButton = findViewById(R.id.location_btn)

        startButton = findViewById(R.id.start_btn)

        location = LocationProcessing(this, mapView)

        location.enableLocationServices()

        locationButton.setOnClickListener {
            location.moveToUserLocation()
        }

        startButton.setOnClickListener {
            location.getTextLocation()
        }
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