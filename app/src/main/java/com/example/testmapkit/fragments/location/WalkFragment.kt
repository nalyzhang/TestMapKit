package com.example.testmapkit.fragments.location

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.MainActivity
import com.example.testmapkit.R
import com.example.testmapkit.TAG
import com.example.testmapkit.databinding.FragmentWalkBinding
import com.example.testmapkit.models.LocationData
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository
import com.example.testmapkit.repositories.RouteRepository
import com.example.testmapkit.services.ChronometerService
import java.util.Locale
import kotlin.getValue

class WalkFragment : Fragment() {

    lateinit var binding: FragmentWalkBinding
    private var chronometerService: ChronometerService? = null
    private var startLocation: LocationData? = null
    private var finishLocation: LocationData? = null

    private lateinit var routeViewModel: RouteViewModel
    private lateinit var tokenManager: TokenManager
    private val sharedLocationsViewModel: SharedLocationsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalkBinding.inflate(
            layoutInflater, container, false)
        // БЕЗОПАСНОЕ получение данных из аргументов
        arguments?.let { bundle ->
            startLocation = bundle.getSerializable("start_location") as? LocationData
            finishLocation = bundle.getSerializable("finish_location") as? LocationData

            Log.d(TAG, "Bundle start_location: ${startLocation != null}")
            Log.d(TAG, "Bundle finish_location: ${finishLocation != null}")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val routeRepository = RouteRepository(retrofitClient.apiService)
        val historyRepository = HistoryRepository(retrofitClient.apiService)
        routeViewModel = RouteViewModel(routeRepository, historyRepository, tokenManager)

        observeViewModel()

        // Если данных нет в аргументах, пробуем получить из SharedViewModel
        if (startLocation == null || finishLocation == null) {
            startLocation = sharedLocationsViewModel.getStartLocationValue()
            finishLocation = sharedLocationsViewModel.getFinishLocationValue()
        }

        // Если данных все еще нет, показываем ошибку
        if (startLocation == null || finishLocation == null) {
            Log.e(TAG, "КРИТИЧЕСКАЯ ОШИБКА: Локации не переданы!")
            Toast.makeText(
                requireContext(),
                "Ошибка: не удалось загрузить данные маршрута",
                Toast.LENGTH_LONG
            ).show()
        }

        setAddress()
        Log.d(TAG, "${startLocation.toString()}, ${finishLocation.toString()}")

        // Получаем сервис из Activity
        chronometerService = (requireActivity() as MainActivity).getChronometerService()

        // Наблюдаем за временем
        chronometerService?.timeLiveData?.observe(viewLifecycleOwner) { time ->
            updateTimeDisplay(time)
        }

        // Запускаем хронометр
        val dateTime = chronometerService?.startChronometer()
        if (dateTime != null) startLocation?.setDateTime(dateTime)

        binding.btnEnd.setOnClickListener {
            finishRoute()
        }
    }

    private fun setAddress() {
        binding.textAddress.text = finishLocation?.getStringAddress()
    }

    private fun updateTimeDisplay(timeInMillis: Long) {
        val hours = (timeInMillis / 3600000).toInt()
        val minutes = ((timeInMillis % 3600000) / 60000).toInt()
        val seconds = ((timeInMillis % 60000) / 1000).toInt()

        val formattedTime = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours, minutes, seconds)
        binding.chronometerWalk.text = formattedTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "WalkFragment destroyed")
    }

    private fun finishRoute() {
        val dateTime = chronometerService?.stopChronometer()
        if (dateTime != null) finishLocation?.setDateTime(dateTime)
        Log.d(TAG, "Завершение прогулки")
        if (startLocation == null || finishLocation == null) return
        val bundle = Bundle().apply {
            // Передаем начальную локацию
            putSerializable("start_location", startLocation)
            // Передаем конечную локацию
            putSerializable("finish_location", finishLocation)
            // Или передаем список
            putSerializable("locations", arrayListOf(startLocation, finishLocation))
        }
        findNavController().navigate(
            R.id.action_walkFragment_to_finishFragment,
            bundle
        )
    }

    private fun observeViewModel() {
        sharedLocationsViewModel.startLocation.observe(viewLifecycleOwner) { location ->
            startLocation = location
            Log.d(TAG, "Start location updated: ${location?.getAddress()}")
        }

        sharedLocationsViewModel.finishLocation.observe(viewLifecycleOwner) { location ->
            finishLocation = location
            setAddress()
            Log.d(TAG, "Finish location updated: ${location?.getAddress()}")
        }
    }
}