package com.example.testmapkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.ADDRESS
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentWalkBinding

class WalkFragment : Fragment() {

    lateinit var binding: FragmentWalkBinding

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
        binding.textAddress.text = address
        binding.btnEnd.setOnClickListener {
            findNavController().navigate(R.id.action_walkFragment_to_locationFragment)
        }
    }
}