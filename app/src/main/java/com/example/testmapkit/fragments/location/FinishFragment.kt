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
import com.example.testmapkit.repositories.FriendResult
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.repositories.RouteResult
import com.example.testmapkit.repositories.UserResult

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
        saveRoute()
        observeViewModel()


        binding.btnFinish.setOnClickListener {
            findNavController().navigate(R.id.action_finishFragment_to_locationFragment)
        }
    }

    private fun setTextViews() {
        binding.tvFinishRouteStartLocation.text = startLocation?.getAddress()

        var time = timeController.extractDateTime(
            startLocation?.getDateTime().toString())
        binding.tvFinishRouteStartTime.text = time
        binding.tvFinishRouteFinishLocation.text = finishLocation?.getAddress()

        time = timeController.extractDateTime(
            finishLocation?.getDateTime().toString())
        binding.tvFinishRouteFinishTime.text = time

        time = timeController.getTimeDifference(
            startLocation?.getDateTime().toString(),
            finishLocation?.getDateTime().toString()
        )
        binding.tvFinishRouteDistance.text = calculateDistance().toString()
        binding.tvFinishRouteTime.text = time
        binding.tvFinishRouteRadius.text = startLocation?.circleRadius.toString()
    }

    private fun saveRoute() {
        // TODO сохранение локаций и маршрута
    }

    private fun calculateDistance(): Double {
        // TODO вычисление расстояния
        return 0.0
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        routeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за состоянием сохранения локации
        routeViewModel.locationCreationState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is RouteResult.Loading -> showLoading(true)
                is RouteResult.Success -> {
                    Log.d(TAG, "Локация сохранена")
                }
                is RouteResult.Error -> {
                    routeViewModel.clearError()
                }
                null -> {}
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