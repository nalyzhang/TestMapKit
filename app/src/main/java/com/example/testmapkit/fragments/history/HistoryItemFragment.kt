package com.example.testmapkit.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentHistoryDetailBinding

class HistoryItemFragment : Fragment() {

    lateinit var binding: FragmentHistoryDetailBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        // TODO("Not yet implemented")
        binding.btnBackHistory.setOnClickListener {
            findNavController().navigate(R.id.action_historyItemFragment_to_historyFragment)
        }
    }
}