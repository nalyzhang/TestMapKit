package com.example.testmapkit.fragments

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.DEFAULT_RADIUS_KM
import com.example.testmapkit.MAIN
import com.example.testmapkit.MAX_RADIUS_KM
import com.example.testmapkit.MIN_RADIUS_KM
import com.example.testmapkit.R
import com.example.testmapkit.RADIUS_SCALE_FACTOR
import com.example.testmapkit.RADIUS_TEXT
import com.example.testmapkit.controllers.LocationController
import com.example.testmapkit.databinding.FragmentLocationBinding
import com.yandex.mapkit.MapKitFactory
import java.util.Locale

class LocationFragment : Fragment() {

    lateinit var binding: FragmentLocationBinding
    private lateinit var location: LocationController
    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(requireContext())
        binding.radiusSizeBar.min = MIN_RADIUS_KM
        binding.radiusSizeBar.max = MAX_RADIUS_KM
        binding.radiusSizeBar.progress = DEFAULT_RADIUS_KM

        binding.radiusSizeText.text = String.Companion.format(
            RADIUS_TEXT,
            binding.radiusSizeBar.progress * RADIUS_SCALE_FACTOR
        )

        location = LocationController(MAIN, binding.mapView)

        location.enableLocationServices()

        binding.locationBtn.setOnClickListener {
            location.moveToUserLocation()
        }

        geocoder = Geocoder(MAIN, Locale.getDefault())

        binding.startBtn.setOnClickListener {
            try {
                val randomLocation = location.getTextLocation()
                if (randomLocation != null) {
//                    val randomPoint = Point(randomLocation.latitude, randomLocation.longitude)
//                    binding.mapView.mapWindow.map.move(
//                        CameraPosition(randomPoint, 15.0f, 0.0f, 0.0f)
//                    )
//                    binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
//                        geometry = randomPoint
//                        val drawable = AppCompatResources.getDrawable(
//                            MAIN,
//                            R.drawable.baseline_location_on_24
//                        )
//                        if (drawable != null) {
//                            val bitmap =
//                                createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
//                            val canvas = Canvas(bitmap)
//                            drawable.setBounds(0, 0, canvas.width, canvas.height)
//                            drawable.draw(canvas)
//
//                            setIcon(ImageProvider.fromBitmap(bitmap))
//                        }
//                        zIndex = 100f
//                    }

                    Log.d(
                        "MainActivity",
                        "latitude: ${randomLocation.latitude} longitude: ${randomLocation.longitude}"
                    )
                    val address = geocoder.getFromLocation(
                        randomLocation.latitude,
                        randomLocation.longitude,
                        1
                    )

                    val bundle = Bundle().apply {
                        putString(ADDRESS, "${address?.get(0)?.getAddressLine(0)}")
                    }

                    findNavController().navigate(
                        R.id.action_locationFragment_to_walkFragment,
                        bundle
                    )
                }
            } catch (e: Exception) {
                Log.d("MainActivity", "$e")
            }
        }

        binding.radiusSizeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    radiusCircleBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.radiusSizeText.text = String.Companion.format(
                        RADIUS_TEXT,
                        progress * RADIUS_SCALE_FACTOR
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
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
        location.startLocationUpdates()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        location.stopLocationUpdates()
        super.onStop()
    }


}