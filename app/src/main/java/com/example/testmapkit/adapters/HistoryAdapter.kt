package com.example.testmapkit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.dataModels.Route
import com.example.testmapkit.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val route = routes[position]

        with(viewHolder) {
            binding.tvRouteName.text = "${route.start.address} -> ${route.finish.address}"

            val timeController = TimeController()

            binding.tvRouteDate.text = timeController.formatDate(route.date)

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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return routes.sortedWith(compareBy { route ->
        try {
            dateFormat.parse(route.date)
        } catch (e: Exception) {
            Date(0) // Если ошибка парсинга, ставим в начало/конец
        }
    }).reversed()
    }
}