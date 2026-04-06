package com.example.testmapkit.fragments.location

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Canvas
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.DEFAULT_RADIUS_KM
import com.example.testmapkit.MAIN
import com.example.testmapkit.MAX_RADIUS_KM
import com.example.testmapkit.MIN_RADIUS_KM
import com.example.testmapkit.PROCESSING
import com.example.testmapkit.R
import com.example.testmapkit.RADIUS_SCALE_FACTOR
import com.example.testmapkit.RADIUS_TEXT
import com.example.testmapkit.controllers.CircleController
import com.example.testmapkit.databinding.FragmentLocationBinding
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.services.LocationService
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider

class LocationFragment : Fragment() {

    lateinit var binding: FragmentLocationBinding
    private var locationService: LocationService? = null
    private var isServiceBound = false
    private var userLocationLayer: UserLocationLayer? = null
    private lateinit var circleController: CircleController
    private var currentLocation: Location? = null

    private var updateCamera: Boolean = true


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
        init()
    }

    private fun init() {
        circleController = CircleController(MAIN)

        setupViews()
        setupUserLocationLayer()
        startLocationService()
    }

    private val serviceListener = object : LocationService.LocationUpdateListener {
        override fun onLocationUpdated(location: Location) {

            currentLocation = location

            if (updateCamera && !PROCESSING) {
                updateMapWithLocation(location)
                updateCamera = false
            }

            // Обновляем круг, используя существующий locationController?
            // Но теперь locationController в сервисе, поэтому нужно либо:
            // 1. Передавать радиус в сервис и там обновлять круг
            // 2. Или дублировать логику круга здесь (проще)
        }

        override fun onGoalReached() {
            // TODO: Обработка достижения цели
//            findNavController().navigate(R.id.action_locationFragment_to_resultFragment)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isServiceBound = true

            // Подписываемся на обновления
            locationService?.addLocationListener(serviceListener)

            // Получаем текущую позицию если есть
            locationService?.getCurrentLocation()?.let { location ->
                updateMapWithLocation(location)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService?.removeLocationListener(serviceListener)
            locationService = null
            isServiceBound = false
        }
    }

    private fun setupViews() {
        binding.radiusSizeBar.apply {
            min = MIN_RADIUS_KM
            max = MAX_RADIUS_KM
            progress = DEFAULT_RADIUS_KM
        }

        binding.radiusSizeText.text = String.Companion.format(
            RADIUS_TEXT,
            binding.radiusSizeBar.progress * RADIUS_SCALE_FACTOR
        )

        binding.radiusSizeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateRadiusText(progress)
                    // Обновляем круг при изменении радиуса
                    currentLocation?.let { location ->
                        circleController.updateRadius(progress, location, binding.mapView.mapWindow.map)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

        binding.startBtn.setOnClickListener {
            startQuest()
        }

        binding.locationBtn.setOnClickListener {
            currentLocation?.let { updateMapWithLocation(it) }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateRadiusText(progress: Int) {
        binding.radiusSizeText.text = String.Companion.format(
            RADIUS_TEXT,
            progress * RADIUS_SCALE_FACTOR
        )
    }

    private fun setupUserLocationLayer() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapView.mapWindow)
        userLocationLayer?.apply {
            isVisible = true
        }
    }

    private fun startLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().startService(intent)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startQuest() {
        // Получаем выбранный радиус
        val radius = binding.radiusSizeBar.progress / 10

        // Отправляем команду сервису
        Intent(requireContext(), LocationService::class.java).also { intent ->
            intent.action = "START_QUEST"
            intent.putExtra("radius", radius)
            requireContext().startService(intent)
        }

        currentLocation?.let { location ->
            circleController.fixCircle()
        }

        // Получаем случайный адрес через сервис
        val randomAddress = locationService?.getRandomAddress(radius)

        if (randomAddress != null) {

            printPoint(randomAddress)

            // Переходим к WalkFragment с адресом
            val bundle = Bundle().apply {
                putString(ADDRESS, randomAddress.getAddress().getAddressLine(0))
            }

            findNavController().navigate(
                R.id.action_locationFragment_to_walkFragment,
                bundle
            )
        } else {
            Toast.makeText(
                requireContext(),
                "Локация еще не определена",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateMapWithLocation(location: Location) {
        val point = location.position
        binding.mapView.mapWindow.map.move(
            CameraPosition(point, 16.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0.5f),
            null
        )
        circleController.drawCircle(location, binding.mapView.mapWindow.map)
    }

    override fun onStart() {
        super.onStart()
        Log.d("LocationFragment", "onStart - calling mapView.onStart()")
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LocationFragment", "onResume - starting map")
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
        updateCamera = true
    }

    override fun onPause() {
        super.onPause()
        Log.d("LocationFragment", "onPause - stopping map")
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStop() {
        super.onStop()
        Log.d("LocationFragment", "onStop")
        // Не вызываем onStop у mapView, так как он уже вызван в onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LocationFragment", "onDestroy")
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    fun printPoint(randomLocation: LocationData) {
        val randomPoint = Point(randomLocation.latitude, randomLocation.longitude)
        binding.mapView.mapWindow.map.move(
            CameraPosition(randomPoint, 15.0f, 0.0f, 0.0f)
        )
        binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = randomPoint
            val drawable = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_location
            )
            if (drawable != null) {
                val bitmap =
                    createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                setIcon(ImageProvider.fromBitmap(bitmap))
            }
            zIndex = 100f
        }
    }
}