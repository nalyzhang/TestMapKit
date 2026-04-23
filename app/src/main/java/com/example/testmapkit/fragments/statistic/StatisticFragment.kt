package com.example.testmapkit.fragments.statistic

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.STATISTIC
import com.example.testmapkit.TAG
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.dataModels.FriendStatistic
import com.example.testmapkit.databinding.FragmentStatisticBinding
import com.example.testmapkit.models.PeriodData
import com.example.testmapkit.models.PeriodType
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.StatisticRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.ZoneId

class StatisticFragment : Fragment() {
    lateinit var binding: FragmentStatisticBinding

    private lateinit var statisticViewModel: StatisticViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var barChart: BarChart
    private lateinit var barDataSet1: BarDataSet
    private lateinit var barDataSet2: BarDataSet
    private lateinit var barEntriesList: ArrayList<BarEntry>

    private lateinit var date: LocalDate
    private lateinit var periodData: PeriodData
    private val timeController = TimeController()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val statisticRepository = StatisticRepository(retrofitClient.apiService)
        statisticViewModel = StatisticViewModel(statisticRepository, tokenManager)

        STATISTIC = true

        init()
    }

    private fun init() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"))
        periodData = PeriodData(
            date, date, PeriodType.DAY
        )

        barChart = binding.bcStatistic
        showLoading(true)
        performDate()
        observeViewModel()
        statisticViewModel.getFriendsStatistics(
            periodData.dateFrom.toString(),
            periodData.dateTo.toString()
        )

        binding.btnStatisticDay.setOnClickListener {
            performDate()
        }

        binding.btnStatisticWeek.setOnClickListener {
            performWeek()
        }

        binding.btnStatisticMonth.setOnClickListener {
            performMonth()
        }

        binding.btnStatisticYear.setOnClickListener {
            performYear()
        }

        binding.btnStatisticToday.setOnClickListener {
            performToday()
        }

        binding.btnStatisticBackArrow.setOnClickListener {
            getPreviousPeriod()
        }

        binding.btnStatisticForwardArrow.setOnClickListener {
            getNextPeriod()
        }

        binding.btnMyStatistic.setOnClickListener {

            // TODO bundle

            findNavController().navigate(
                R.id.action_statisticFragment_to_userStatisticFragment
            )
        }
    }

    private fun performDate() {
        periodData.setDay(date)
        setPeriod()
        binding.tvStatisticPeriod.text = setTextDay()
    }

    private fun performWeek() {
        periodData.setWeek(date)
        setPeriod()
        binding.tvStatisticPeriod.text = setTextWeek()
    }

    private fun performMonth() {
        periodData.setMonth(date)
        setPeriod()
        binding.tvStatisticPeriod.text = setTextMonth()
    }

    private fun performToday() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"))
        when (periodData.periodType) {
            PeriodType.DAY -> {
                performDate()
            }
            PeriodType.WEEK -> {
                performWeek()
            }
            PeriodType.MONTH -> {
                performMonth()
            }
            PeriodType.YEAR -> {
                performYear()
            }
            else -> {}
        }
    }

    private fun performYear(){
        periodData.setYear(date)
        setPeriod()
        binding.tvStatisticPeriod.text = setTextYear()
    }

    private fun initBar(
        xData: ArrayList<String>,
        statistic: List<FriendStatistic>
    ) {
        barDataSet1 = BarDataSet(
            getBarEntries(statistic, true),
            "Кол-во завершенных маршрутов"
        ).apply {
            color = Color.GREEN
            valueTextSize = 11f
            valueTextColor = Color.BLACK
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        barDataSet2 = BarDataSet(
            getBarEntries(statistic, false),
            "Кол-во незавершенных маршрутов"
        ).apply {
            color = Color.RED
            valueTextSize = 11f
            valueTextColor = Color.BLACK
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        val data = BarData(barDataSet1, barDataSet2)
        barChart.data = data
        barChart.apply {
            description.isEnabled = false
            isDragEnabled = true
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false

            // Уменьшаем отступы
            setExtraOffsets(10f, 10f, 10f, 10f)

            // Настройка легенды
            legend.isEnabled = true
            legend.textSize = 12f
            legend.formSize = 12f
            legend.xEntrySpace = 10f
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.yOffset = 10f
            legend.isWordWrapEnabled = true
            legend.maxSizePercent = 0.8f
        }

        // Настройка оси X
        val xAxis: XAxis = barChart.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(xData)
            granularity = 1f
            isGranularityEnabled = true
            //setDrawGridLines(false)
            textSize = 12f
            labelRotationAngle = if (xData.size > 5) 45f else 0f
            setCenterAxisLabels(true)
            setAvoidFirstLastClipping(true)

            // Устанавливаем количество меток
            labelCount = xData.size
        }

        // Настройка левой оси Y
        barChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            setDrawGridLines(true)
            gridLineWidth = 0.5f
            gridColor = Color.LTGRAY
            textSize = 12f

            // Форматируем значения на оси Y как целые числа
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        // Отключаем правую ось
        barChart.axisRight.isEnabled = false

        // Рассчитываем параметры группировки в зависимости от количества данных
        val groupCount = statistic.size.toFloat()

        val groupSpace = 0.5f

        val barSpace = 0.1f

        data.barWidth = 0.15f

        // Группируем бары
        barChart.groupBars(0f, groupSpace, barSpace)

        // Устанавливаем границы оси X
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = groupCount

        // Анимация
        barChart.animateY(1000)

        // Обновляем график
        barChart.invalidate()
    }

    private fun getBarEntries(
        statistic: List<FriendStatistic>,
        completed: Boolean
    ): ArrayList<BarEntry> {
        barEntriesList = ArrayList()

        // on below line we are adding data
        // to our bar entries list
        var i = 1f
        for (friendStatistic in statistic) {
            if (completed)
                barEntriesList.add(
                    BarEntry(
                        i,
                        (friendStatistic.completedRoutesCount
                                ).toFloat()
                    )
                )
            else
                barEntriesList.add(
                    BarEntry(
                        i,
                        (friendStatistic.routesCount - friendStatistic.completedRoutesCount
                                ).toFloat()
                    )
                )
            i += 1
        }
        return barEntriesList
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        statisticViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными пользователя
        statisticViewModel.friendsStatistic.observe(viewLifecycleOwner) { statistic ->
            if (statistic != null) {
                updateBar(statistic.results)
                showLoading(false)
            }
        }

        // Наблюдаем за ошибками
        statisticViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                statisticViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun updateBar(statistic: List<FriendStatistic>) {
        val xData: ArrayList<String> = arrayListOf()
        for (friendStatistic in statistic) {
            xData.add(friendStatistic.user.username)
        }

        Log.d(TAG, "$xData")

        initBar(xData, statistic)
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbStatistic.visibility = View.VISIBLE
            binding.bcStatistic.visibility = View.GONE
        } else {
            binding.pbStatistic.visibility = View.GONE
            binding.bcStatistic.visibility = View.VISIBLE
        }
    }

    private fun setTextDay(): String {
        return timeController.formatDate(date.toString())
    }

    private fun setTextWeek(): String {
        Log.d(TAG, date.toString())
        val from = timeController.formatDate(
            periodData.dateFrom.toString())
        val to = timeController.formatDate(
            periodData.dateTo.toString())
        return "$from - $to"
    }

    private fun setTextMonth(): String {
        Log.d(TAG, date.toString())
        return timeController.formatMonth(date)
    }

    private fun setTextYear(): String {
        Log.d(TAG, date.toString())
        return "${timeController.formatYear(date)} год"
    }

    private fun getPreviousPeriod(){
        when (periodData.periodType) {
            PeriodType.DAY -> {
                periodData.previousDay(date)
                date = periodData.dateFrom!!
                performDate()
            }
            PeriodType.WEEK -> {
                periodData.previousWeek(date)
                date = periodData.dateFrom!!
                performWeek()
            }
            PeriodType.MONTH -> {
                periodData.previousMonth(date)
                date = periodData.dateFrom!!
                performMonth()
            }
            PeriodType.YEAR -> {
                periodData.previousYear(date)
                date = periodData.dateFrom!!
                performYear()
            }
            else -> {}
        }
    }

    private fun getNextPeriod(){
        when (periodData.periodType) {
            PeriodType.DAY -> {
                periodData.nextDay(date)
                date = periodData.dateFrom!!
                performDate()
            }
            PeriodType.WEEK -> {
                periodData.nextWeek(date)
                date = periodData.dateFrom!!
                performWeek()
            }
            PeriodType.MONTH -> {
                periodData.nextMonth(date)
                date = periodData.dateFrom!!
                performMonth()
            }
            PeriodType.YEAR -> {
                periodData.nextYear(date)
                date = periodData.dateFrom!!
                performYear()
            }
            else -> {}
        }
    }

    private fun setPeriod() {
        Log.d(TAG, date.toString())
        setEnabled()
        statisticViewModel.getFriendsStatistics(
            periodData.dateFrom.toString(),
            periodData.dateTo.toString()
        )
    }

    private fun setEnabled() {
        binding.btnStatisticDay.isEnabled = (periodData.periodType != PeriodType.DAY)
        binding.btnStatisticWeek.isEnabled = (periodData.periodType != PeriodType.WEEK)
        binding.btnStatisticMonth.isEnabled = (periodData.periodType != PeriodType.MONTH)
        binding.btnStatisticYear.isEnabled = (periodData.periodType != PeriodType.YEAR)
    }

}