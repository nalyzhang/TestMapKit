package com.example.testmapkit.fragments.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmapkit.R
import com.example.testmapkit.ROUTE_ID
import com.example.testmapkit.TAG
import com.example.testmapkit.adapters.HistoryAdapter
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.databinding.FragmentHistoryBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.HistoryRepository

class HistoryFragment : Fragment() {

    lateinit var binding: FragmentHistoryBinding
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var tokenManager: TokenManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val historyRepository = HistoryRepository(retrofitClient.apiService)
        historyViewModel = HistoryViewModel(historyRepository, tokenManager)

        recyclerView = binding.rvHistory
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        init()
        observeViewModel()
        loadHistoryList()
    }

    private fun init() {
        historyAdapter = HistoryAdapter(emptyList()) { routeID ->

            val bundle = Bundle().apply {
                putInt(ROUTE_ID, routeID)
            }
            findNavController().navigate(
                R.id.action_historyFragment_to_historyItemFragment,
                bundle
            )
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        binding.searchHistory.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                Log.d(TAG, "onQueryTextChange")
                observeViewModel()
                historyAdapter.findItem(query)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d(TAG, "onQueryTextSubmit")
                binding.searchHistory.hideKeyboard()
                binding.searchHistory.clearFocus()
                observeViewModel()
                historyAdapter.findItem(query)
                return false
            }
        })

        binding.root.setOnClickListener {
            if (binding.searchHistory.hasFocus()) {
                binding.searchHistory.hideKeyboard()
                binding.searchHistory.clearFocus()
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными истории
        historyViewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            if (historyList != null) {
                updateHistoryList(historyList)
                showLoading(false)
            }
        }

        // Наблюдаем за ошибками
        historyViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                historyViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbHistoryList.visibility = View.VISIBLE
            binding.rvHistory.visibility = View.GONE
        } else {
            binding.pbHistoryList.visibility = View.GONE
            binding.rvHistory.visibility = View.VISIBLE
        }
    }

    private fun loadHistoryList() {
        if (tokenManager.hasToken()) {
            historyViewModel.getMyRoutes()
        } else {
            updateHistoryList(emptyList())
        }
    }

    private fun updateHistoryList(historyList: List<Route>) {
        if (historyList.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.tvEmptyHistoryList.visibility = View.VISIBLE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.tvEmptyHistoryList.visibility = View.GONE
            historyAdapter.updateData(historyList)
        }
    }

    override fun onResume() {
        super.onResume()
        if (tokenManager.hasToken()) {
            loadHistoryList()
        }
    }
}