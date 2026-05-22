package com.example.testmapkit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testmapkit.R
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.databinding.ItemHistoryBinding
import com.example.testmapkit.models.LocationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.getDefault

class HistoryAdapter(
    private var routes: List<Route>,
    private val onItemClick: ((userId: Int) -> Unit)? = null
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    private fun returnRouteName(route: Route): String {
        val startLocation = LocationData(
            0.0, 0.0
        )
        startLocation.setAddress(route.start.address)
        val finishLocation = LocationData(
            0.0, 0.0
        )
        if (route.stop?.address != null)
            finishLocation.setAddress(route.stop.address)
        else
            finishLocation.setAddress(route.finish.address)
        return "${startLocation.getStringAddress()} -> ${finishLocation.getStringAddress()}"
    }

    private fun returnDateAndTownName(route: Route): String {
        val startLocation = LocationData(
            0.0, 0.0
        )
        startLocation.setAddress(route.start.address)
        val timeController = TimeController()
        val time = timeController.formatDate(route.date)
        return "$time, ${startLocation.getTown()}"
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val route = routes[position]

        with(viewHolder) {
            binding.tvRouteName.text = returnRouteName(route)

            binding.tvRouteDate.text = returnDateAndTownName(route)

            if (route.stop?.address != null)
                binding.imgHistoryList.setImageResource(R.drawable.ic_not_done)
            else
                binding.imgHistoryList.setImageResource(R.drawable.ic_done)

            // Обработка нажатия на элемент
            binding.root.setOnClickListener {
                onItemClick?.invoke(route.id)
            }
        }
    }

    override fun getItemCount() = routes.size

    fun updateData(newRoutes: List<Route>) {
        routes = sortRoutes(newRoutes)
        notifyDataSetChanged()
    }

    private fun sortRoutes(routes: List<Route>): List<Route> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", getDefault())

        return routes.sortedWith(compareBy { route ->
            try {
                dateFormat.parse(route.date)
            } catch (e: Exception) {
                Date(0) // Если ошибка парсинга, ставим в начало/конец
            }
        }).reversed()
    }

    fun findItem(query: String) {
        val foundRoutes: MutableList<Route> = mutableListOf()

        val timeController = TimeController()

        query.lowercase(getDefault())

        for (route in routes) {
            val name = returnRouteName(route).lowercase(getDefault())
            val time = timeController.formatDate(
                route.date).lowercase(getDefault())
            if (name.contains(query.lowercase(getDefault())) ||
                route.date.lowercase(
                    getDefault()).contains(
                    query.lowercase(getDefault())) ||
                time.contains(query.lowercase(getDefault()))) {
                foundRoutes += route
            }
        }
        updateData(foundRoutes)
    }
}