package com.example.testmapkit.fragments.location

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.TAG
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.databinding.FragmentFinishBinding
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.repositories.RouteResult
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.geo.PolylineIndex
import com.yandex.mapkit.geometry.geo.PolylineUtils
import java.util.Locale

class FinishFragment : Fragment() {

    private lateinit var binding: FragmentFinishBinding
    private var startLocation: LocationData? = null
    private var finishLocation: LocationData? = null
    private val timeController = TimeController()

    private lateinit var routeViewModel: RouteViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinishBinding.inflate(
            layoutInflater, container, false)
        startLocation = arguments?.getSerializable("start_location") as LocationData
        finishLocation = arguments?.getSerializable("finish_location") as LocationData
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val routeRepository = RouteRepository(retrofitClient.apiService)
        val historyRepository = HistoryRepository(retrofitClient.apiService)
        routeViewModel = RouteViewModel(routeRepository, historyRepository, tokenManager)

        init()
    }

    private fun init() {
        setTextViews()
        observeViewModel()


        binding.btnFinish.setOnClickListener {
            findNavController().navigate(R.id.action_finishFragment_to_locationFragment)
        }
    }

    private fun setTextViews() {
        binding.tvFinishRouteStartLocation.text = startLocation?.getStringAddress()

        var time = timeController.extractTime(
            startLocation?.getDateTime().toString())
        binding.tvFinishRouteStartTime.text = time
        binding.tvFinishRouteFinishLocation.text = finishLocation?.getStringAddress()

        time = timeController.extractTime(
            finishLocation?.getDateTime().toString())
        binding.tvFinishRouteFinishTime.text = time

        time = timeController.getTimeDifference(
            startLocation?.getDateTime().toString(),
            finishLocation?.getDateTime().toString()
        )
        binding.tvFinishRouteDate.text = timeController.extractDate(
            startLocation?.getDateTime().toString())
        binding.tvFinishRouteDistance.text = stringDistance()
        binding.tvFinishRouteTime.text = time
        binding.tvFinishRouteRadius.text = startLocation?.getStringRadius()


        if (startLocation != null && finishLocation != null) {
            saveLocation()
        }
    }

    private fun saveLocation() {
        if (tokenManager.hasToken()) {
            routeViewModel.createLocation(
                startLocation!!,
                finishLocation!!
            )
        } else {
            Toast.makeText(
                requireContext(),
                "Невозможно сохранить маршрут, пользователь не авторизован",
                Toast.LENGTH_SHORT
            ).show()
            showLoading(false)
        }
    }

    private fun saveRoute(startLocationID: Int, finishLocationID: Int) {
        val distance: Double = calculateDistance()
        val time = binding.tvFinishRouteTime.text.toString().trim()
        val date = timeController.formatDateReverse(
            binding.tvFinishRouteDate.text.toString().trim()
        )

        routeViewModel.createRoute(
            startLocationID,
            finishLocationID,
            distance,
            time,
            date
        )
    }

    private fun stringDistance(): String {
        return "${calculateDistance()} км"
    }

    private fun calculateDistance(): Double {
        if (startLocation != null && finishLocation != null) {
            val firstPoint = Point(
                startLocation!!.latitude,
                startLocation!!.longitude
            )

            val secondPoint = Point(
                finishLocation!!.latitude,
                finishLocation!!.longitude
            )

            val polyline = Polyline(listOf(firstPoint, secondPoint))
            val polylineIndex = PolylineUtils.createPolylineIndex(polyline)

            val firstPosition = polylineIndex.closestPolylinePosition(
                firstPoint,
                PolylineIndex.Priority.CLOSEST_TO_RAW_POINT,
                1.0
            )!!
            val secondPosition = polylineIndex.closestPolylinePosition(
                secondPoint,
                PolylineIndex.Priority.CLOSEST_TO_RAW_POINT,
                1.0
            )!!

            val distanceInMeters = PolylineUtils.distanceBetweenPolylinePositions(
                polyline,
                firstPosition,
                secondPosition
            )

            return String.format(
                Locale.US,
                "%.2f",
                (distanceInMeters / 1000.0)
            ).toDouble()
        }
        return 0.0
    }
    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        routeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за состоянием сохранения локации
        routeViewModel.locationCreationState.observe(viewLifecycleOwner) { result ->
            if (result?.get(0)?.id != null) {
                saveRoute(
                    result[0].id,
                    result[1].id
                )
            }
        }

        // Наблюдаем за состоянием сохранения маршрута
        routeViewModel.routeCreationState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is RouteResult.Loading -> showLoading(true)
                is RouteResult.Success -> {
                    Log.d(TAG, "Маршрут сохранен")
                }
                is RouteResult.Error -> {
                    routeViewModel.clearError()
                }
                null -> {}
            }
        }

        // Наблюдаем за ошибками
        routeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                routeViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbFinish.visibility = View.VISIBLE
            binding.llFinish.visibility = View.GONE
            binding.btnFinish.visibility = View.GONE
        } else {
            binding.pbFinish.visibility = View.GONE
            binding.llFinish.visibility = View.VISIBLE
            binding.btnFinish.visibility = View.VISIBLE
        }
    }
}