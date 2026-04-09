package com.example.testmapkit.fragments.location

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentWalkBinding
import com.example.testmapkit.services.ChronometerService

class WalkFragment : Fragment() {

    lateinit var binding: FragmentWalkBinding
    private var chronometerService: ChronometerService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChronometerService.ChronometerBinder
            chronometerService = binder.getService()
            isBound = true

            // Восстанавливаем отображение времени
            chronometerService?.timeLiveData?.observe(viewLifecycleOwner) { time ->
                updateTimeDisplay(time)
            }

            // Если нужно автоматически запустить при создании фрагмента
            chronometerService?.startChronometer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            chronometerService = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalkBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val address = arguments?.getString(ADDRESS)
        if (address != null) binding.textAddress.text = address

        // Запускаем сервис
        val intent = Intent(requireContext(), ChronometerService::class.java)
        requireContext().startService(intent)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        binding.btnEnd.setOnClickListener {
            chronometerService?.stopChronometer()
            findNavController().navigate(R.id.action_walkFragment_to_finishFragment)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimeDisplay(timeInMillis: Long) {
        val hours = (timeInMillis / 3600000).toInt()
        val minutes = ((timeInMillis % 3600000) / 60000).toInt()
        val seconds = ((timeInMillis % 60000) / 1000).toInt()

        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.chronometerWalk.text = formattedTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            requireContext().unbindService(serviceConnection)
            isBound = false
        }
    }
}