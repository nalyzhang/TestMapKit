package com.example.testmapkit.fragments.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.ROUTE_ID
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.databinding.FragmentHistoryDetailBinding
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.HistoryResult

class HistoryItemFragment : Fragment() {

    lateinit var binding: FragmentHistoryDetailBinding
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var tokenManager: TokenManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val historyRepository = HistoryRepository(retrofitClient.apiService)
        historyViewModel = HistoryViewModel(historyRepository, tokenManager)

        val routeID = arguments?.getInt(ROUTE_ID)
        if (routeID != null) loadRoute(routeID)

        init(routeID)
        observeViewModel()
    }

    private fun init(routeID: Int?) {
        binding.btnBackHistory.setOnClickListener {
            findNavController().navigate(
                R.id.action_historyItemFragment_to_historyFragment)
        }

        binding.btnDeleteHistoryItem.setOnClickListener {
            if (routeID != null) showUpdateConfirmationDialog(routeID)
        }
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными пользователя
        historyViewModel.routeByID.observe(viewLifecycleOwner) { route ->
            if (route != null) {
                updateRoute(route)
                showLoading(false)
            }
        }

        // Наблюдаем за результатом удаления друга
        historyViewModel.removeRouteState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is HistoryResult.Loading -> {
                    showLoading(true)
                }
                is HistoryResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Пользователь удален из друзей",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is HistoryResult.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }

        // Наблюдаем за ошибками
        historyViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                historyViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun updateRoute(route: Route) {
        val tc = TimeController()
        val startLocation = LocationData(
            0.0, 0.0
        )
        startLocation.setAddress(route.start.address)
        val finishLocation = LocationData(
            0.0, 0.0
        )

        val stopLocation = LocationData(
            0.0, 0.0
        )

        if (route.stop?.address != null) {
            finishLocation.setAddress(route.finish.address)
            stopLocation.setAddress(route.stop.address)
        }
        else {
            finishLocation.setAddress(route.finish.address)
            stopLocation.setAddress(route.finish.address)
        }
        var temp = "${startLocation.getStringAddress()} -> ${stopLocation.getStringAddress()}"
        binding.tvHistoryRouteName.text = temp
        binding.tvHistoryRouteDestination.text = finishLocation.getStringAddress()
        binding.tvHistoryRouteDate.text = tc.formatDate(route.date)
        temp = "${route.distance} км"
        binding.tvHistoryRouteDistance.text = temp
        temp = "${route.start.radius.toString()} км"
        binding.tvHistoryRouteRadius.text = temp
        binding.tvHistoryRouteTime.text = route.time
        binding.tvHistoryRouteStartLocation.text = startLocation.getStringAddress()
        binding.tvHistoryRouteStartTime.text = tc.extractTime(route.start.time)
        binding.tvHistoryRouteFinishLocation.text = stopLocation.getStringAddress()
        binding.tvHistoryRouteFinishTime.text = tc.extractTime(route.finish.time)
    }

    private fun loadRoute(routeID: Int) {
        if (tokenManager.hasToken()) {
            historyViewModel.getRouteById(routeID)
        } else {
            Toast.makeText(
                requireContext(),
                "Пользователь не авторизован",
                Toast.LENGTH_SHORT
            ).show()
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbHistory.visibility = View.VISIBLE
            binding.llHistoryDetail.visibility = View.GONE
        } else {
            binding.pbHistory.visibility = View.GONE
            binding.llHistoryDetail.visibility = View.VISIBLE
        }
    }

    private fun showUpdateConfirmationDialog(routeID: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить маршрут безвозвратно?")
            .setPositiveButton("Да") { _, _ ->
                historyViewModel.deleteRoute(routeID)
                findNavController().navigate(
                    R.id.action_historyItemFragment_to_historyFragment
                )
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}