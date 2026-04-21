package com.example.testmapkit.fragments.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentUserStatisticBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.StatisticRepository

class UserStatisticFragment : Fragment() {
    private lateinit var binding: FragmentUserStatisticBinding
    private lateinit var statisticViewModel: StatisticViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserStatisticBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val statisticRepository = StatisticRepository(retrofitClient.apiService)
        statisticViewModel = StatisticViewModel(statisticRepository, tokenManager)


        init()
    }

    private fun init () {
        // TODO
    }
}