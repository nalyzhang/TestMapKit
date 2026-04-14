package com.example.testmapkit.fragments.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.work.impl.utils.INITIAL_ID
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.DEFAULT_RADIUS_KM
import com.example.testmapkit.MAIN
import com.example.testmapkit.MAX_RADIUS_KM
import com.example.testmapkit.MIN_RADIUS_KM
import com.example.testmapkit.MainActivity
import com.example.testmapkit.PROCESSING
import com.example.testmapkit.R
import com.example.testmapkit.RADIUS_SCALE_FACTOR
import com.example.testmapkit.RADIUS_TEXT
import com.example.testmapkit.TAG
import com.example.testmapkit.controllers.CircleController
import com.example.testmapkit.databinding.FragmentLocationBinding
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.services.LocationService
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import java.util.Locale

class LocationFragment : Fragment() {

    lateinit var binding: FragmentLocationBinding
    private var locationService: LocationService? = null
    private var userLocationLayer: UserLocationLayer? = null
    private lateinit var circleController: CircleController
    private var currentLocation: Location? = null

    private var updateCamera: Boolean = true
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var tokenManager: TokenManager
    private val sharedLocationsViewModel: SharedLocationsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val routeRepository = RouteRepository(retrofitClient.apiService)
        val historyRepository = HistoryRepository(retrofitClient.apiService)
        routeViewModel = RouteViewModel(routeRepository, historyRepository, tokenManager)

        // Получаем сервис из Activity
        locationService = (requireActivity() as MainActivity).getLocationService()

        // Устанавливаем слушатель
        (requireActivity() as MainActivity).setLocationUpdateListener(serviceListener)

        init()
    }

    private fun init() {
        circleController = CircleController(MAIN)

        setupViews()
        setupUserLocationLayer()

        // Получаем текущую позицию если есть
        locationService?.getCurrentLocation()?.let { location ->
            updateMapWithLocation(location)
        }
    }

    private val serviceListener = object : LocationService.LocationUpdateListener {
        override fun onLocationUpdated(location: Location) {

            currentLocation = location

            if (updateCamera && !PROCESSING) {
                updateMapWithLocation(location)
                updateCamera = false
            }
        }
    }

    private fun setupViews() {
        binding.radiusSizeBar.apply {
            min = MIN_RADIUS_KM
            max = MAX_RADIUS_KM
            progress = DEFAULT_RADIUS_KM
        }

        binding.radiusSizeText.text = String.format(
            Locale.getDefault(),
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

    private fun updateRadiusText(progress: Int) {
        binding.radiusSizeText.text = String.format(
            Locale.getDefault(),
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

    private fun startQuest() {
        // Получаем выбранный радиус
        val radius = binding.radiusSizeBar.progress.toDouble() / 10

        // Отправляем команду сервису
        Intent(requireContext(), LocationService::class.java).also { intent ->
            intent.action = "START_QUEST"
            intent.putExtra("radius", radius)
            requireContext().startService(intent)
        }

        currentLocation?.let { _ ->
            circleController.fixCircle()
            if (currentLocation != null) {
                routeViewModel.getAddresses(currentLocation!!.position, radius)
            }
            else {
                Toast.makeText(
                requireContext(),
                "Локация еще не определена",
                Toast.LENGTH_LONG
            ).show()
            }
        }

        observeViewModel()
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
        (requireActivity() as MainActivity).setLocationUpdateListener(null)
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        routeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        routeViewModel.currentAddressState.observe(viewLifecycleOwner) { location ->
            if (location[0] != null && location[1] != null) {
                showLoading(false)

                Log.d(TAG, "Получен текущий адрес: ${location[0]?.getAddress()}")

                Log.d(TAG, "Получен рандомный адрес: ${location[1]?.getAddress()}")
                sharedLocationsViewModel.setStartLocation(location[0])
                sharedLocationsViewModel.setFinishLocation(location[1])
                moveToWalk(location[0], location[1])
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ошибка поиска, повторите запрос",
                    Toast.LENGTH_LONG
                ).show()
                routeViewModel.clearError()
            }
        }

        // Наблюдаем за ошибками
        routeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                routeViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun moveToWalk(startLocation: LocationData?, finishLocation: LocationData?) {
        Log.d(TAG, "Переход из локации в прогулку")
        if (startLocation == null || finishLocation == null) return
        val currentDestination = findNavController().currentDestination
        if (currentDestination?.id != R.id.locationFragment) {
            Log.d(TAG, "Текущий destination не LocationFragment (id: ${currentDestination?.id}), пропускаем навигацию")
            return
        }
        val bundle = Bundle().apply {
            // Передаем начальную локацию
            putSerializable("start_location", startLocation)
            // Передаем конечную локацию
            putSerializable("finish_location", finishLocation)
        }
        findNavController().navigate(
            R.id.action_locationFragment_to_walkFragment,
            bundle)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbLocation.visibility = View.VISIBLE
            binding.clLocationMain.visibility = View.GONE
        } else {
            binding.pbLocation.visibility = View.GONE
            binding.clLocationMain.visibility = View.VISIBLE
        }
    }
}