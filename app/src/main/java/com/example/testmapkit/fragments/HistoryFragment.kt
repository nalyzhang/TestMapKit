package com.example.testmapkit.fragments

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
import com.example.testmapkit.databinding.FragmentHistoryBinding
import com.yandex.mapkit.MapKitFactory

class HistoryFragment : Fragment() {

    lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    // TODO: история
}