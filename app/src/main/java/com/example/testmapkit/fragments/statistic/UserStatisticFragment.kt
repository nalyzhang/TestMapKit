package com.example.testmapkit.fragments.statistic

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.testmapkit.MAX_RADIUS_KM
import com.example.testmapkit.MIN_RADIUS_KM
import com.example.testmapkit.R
import com.example.testmapkit.TAG
import com.example.testmapkit.controllers.TimeController
import com.example.testmapkit.dataModels.UserStatistic
import com.example.testmapkit.databinding.FragmentUserStatisticBinding
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
import java.util.Locale

class UserStatisticFragment : Fragment() {
    private lateinit var binding: FragmentUserStatisticBinding
    private lateinit var statisticViewModel: StatisticViewModel
    private lateinit var tokenManager: TokenManager

    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart
    private lateinit var barChart3: BarChart
    private lateinit var barDataSet11: BarDataSet
    private lateinit var barDataSet12: BarDataSet
    private lateinit var barDataSet2: BarDataSet
    private lateinit var barDataSet3: BarDataSet
    lateinit var barData3: BarData
    private var barEntriesList: ArrayList<BarEntry> = ArrayList()

    private lateinit var date: LocalDate
    private lateinit var periodData: PeriodData
    private val timeController = TimeController()

    private var gotUserNameAndAvatar: Boolean = false

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

    // TODO arguments, getStatisticByUserID
    private fun init () {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"))
        periodData = PeriodData(
            date, date, PeriodType.WEEK
        )
        periodData.setWeek(date)
        statisticViewModel.getMyStatistic(
            periodData.dateFrom.toString(),
            periodData.dateTo.toString()
        )


        barChart1 = binding.bcStatisticCount
        barChart2 = binding.bcStatisticTime
        barChart3 = binding.bcStatisticRadius
        showLoading(true)
        performWeek()
        observeViewModel()

        binding.btnUserStatisticWeek.setOnClickListener {
            performWeek()
        }

        binding.btnUserStatisticMonth.setOnClickListener {
            performMonth()
        }

        binding.btnUserStatisticYear.setOnClickListener {
            performYear()
        }

        binding.btnUserStatisticToday.setOnClickListener {
            performToday()
        }

        binding.btnUserStatisticBackArrow.setOnClickListener {
            getPreviousPeriod()
        }

        binding.btnUserStatisticForwardArrow.setOnClickListener {
            getNextPeriod()
        }

        binding.btnProfileUserStatistic.setOnClickListener {
            findNavController().navigate(
                R.id.action_userStatisticFragment_to_profileFragment)
        }

        binding.btnBackUserStatistic.setOnClickListener {
            findNavController().navigate(
                R.id.action_userStatisticFragment_to_statisticFragment
            )
        }
    }

    private fun performWeek() {
        periodData.setWeek(date)
        setPeriod()
        binding.tvUserStatisticPeriod.text = setTextWeek()
    }

    private fun performMonth() {
        periodData.setMonth(date)
        setPeriod()
        binding.tvUserStatisticPeriod.text = setTextMonth()
    }

    private fun performToday() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"))
        when (periodData.periodType) {
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
        binding.tvUserStatisticPeriod.text = setTextYear()
    }


    private fun getBarEntriesPeriod(
        statistic: UserStatistic,
        completed: Boolean
    ): ArrayList<BarEntry> {
        barEntriesList = ArrayList()

        if (periodData.periodType == PeriodType.YEAR) {
            return aggregateDaysToMonths(
                statistic.routesCount,
                statistic.completedRoutesCount,
                completed
            )
        }

        // on below line we are adding data
        // to our bar entries list
        for (i in 0..<statistic.routesCount.size) {
            if (completed)
                barEntriesList.add(
                    BarEntry(
                        i.toFloat(),
                        (statistic.completedRoutesCount[i]
                                ).toFloat()
                    )
                )
            else
                barEntriesList.add(
                    BarEntry(
                        i.toFloat(),
                        (statistic.routesCount[i] - statistic.completedRoutesCount[i]
                                ).toFloat()
                    )
                )
        }
        return barEntriesList
    }

    fun aggregateDaysToMonths(routesCount: List<Int>,
                              routesCompletedCount: List<Int>,
                              completed: Boolean): ArrayList<BarEntry> {
        val daysInMonths = if (periodData.isLeapYear(date)) {
            listOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        } else {
            listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        }

        barEntriesList = ArrayList()
        var dayIndex = 0
        var i = 1f

        for (daysInMonth in daysInMonths) {
            var monthSum = 0
            for (day in 0 until daysInMonth) {
                if (dayIndex < routesCount.size) {
                    monthSum += if (completed)
                        routesCompletedCount[dayIndex]
                    else
                        (
                                routesCount[dayIndex] - routesCompletedCount[dayIndex]
                                )
                    dayIndex++
                }
            }
            barEntriesList.add(
                BarEntry(
                    i,
                    monthSum.toFloat()
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

        // Наблюдаем за данными пользователя по ID
        statisticViewModel.statisticByID.observe(viewLifecycleOwner) { statistic ->
            if (statistic != null) {
                initStatistics(statistic)
            }
        }

        // Наблюдаем за данными текущего пользователя
        statisticViewModel.myStatistic.observe(viewLifecycleOwner) { statistic ->
            if (statistic != null) {
                initStatistics(statistic)
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.tvUserStatisticUsername.visibility = View.GONE
            binding.llUserStatisticDetail.visibility = View.GONE
            binding.pbUserStatistic.visibility = View.VISIBLE
        } else {
            binding.tvUserStatisticUsername.visibility = View.VISIBLE
            binding.llUserStatisticDetail.visibility = View.VISIBLE
            binding.pbUserStatistic.visibility = View.GONE
        }
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

        statisticViewModel.getMyStatistic(
            periodData.dateFrom.toString(),
            periodData.dateTo.toString()
        )
    }

    private fun setEnabled() {
        binding.btnUserStatisticWeek.isEnabled = (periodData.periodType != PeriodType.WEEK)
        binding.btnUserStatisticMonth.isEnabled = (periodData.periodType != PeriodType.MONTH)
        binding.btnUserStatisticYear.isEnabled = (periodData.periodType != PeriodType.YEAR)
    }

    private fun initStatistics(statistic: UserStatistic) {

        if (!gotUserNameAndAvatar){
            if (!statistic.user.avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(statistic.user.getFullAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.imgUserStatistic)
            } else {
                binding.imgUserStatistic.setImageResource(R.drawable.ic_profile)
            }
            binding.tvUserStatisticUsername.text = statistic.user.username
        }

        val routesCount = statistic.routesCount.sum()
        val completedRoutesCount = statistic.completedRoutesCount.sum()
        val completedRoutesPercent = "${
            (completedRoutesCount / routesCount * 100)
        } %"
        binding.tvUserStatisticStatCount.text = routesCount.toString()
        binding.tvUserStatisticCompletedCount.text = completedRoutesCount.toString()
        binding.tvUserStatisticCompletedPer.text = completedRoutesPercent
        val averageRadius = "${statistic.averageRadius} км"
        binding.tvUserStatisticRadius.text = averageRadius

        initPeriodStatistic(statistic)
        initTimeStatistic(statistic)
        initRadiusStatistic(statistic)

        showLoading(false)
    }

    private fun initPeriodStatistic(statistic: UserStatistic) {
        barDataSet11 = BarDataSet(
            getBarEntriesPeriod(statistic, true),
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

        barDataSet12 = BarDataSet(
            getBarEntriesPeriod(statistic, false),
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

        val data = BarData(barDataSet11, barDataSet12)
        barChart1.data = data
        barChart1.apply {
            description.isEnabled = false

            // ВКЛЮЧАЕМ ПРОКРУТКУ И МАСШТАБИРОВАНИЕ
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            isDoubleTapToZoomEnabled = true

            // Настройка видимой области (сколько баров показывать изначально)
            setVisibleXRangeMaximum(7f)  // Показываем максимум 7 дней
            setVisibleXRangeMinimum(3f)  // Минимум 3 дня видно

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

            // Дополнительные настройки для скролла
            isDragDecelerationEnabled = true
            dragDecelerationFrictionCoef = 0.9f

            // Подсветка при нажатии
            isHighlightPerDragEnabled = true
        }

        val xData = getListOfDates()
        Log.d(TAG, "Даты для оси X: $xData")

        // Настройка оси X
        val xAxis: XAxis = barChart1.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(xData)
            granularity = 1f
            isGranularityEnabled = true
            textSize = 12f
            labelRotationAngle = if (xData.size > 5) 90f else 0f
            setCenterAxisLabels(true)
            setAvoidFirstLastClipping(true)
            labelCount = xData.size
        }

        // Настройка левой оси Y
        barChart1.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            setDrawGridLines(true)
            gridLineWidth = 0.5f
            gridColor = Color.LTGRAY
            textSize = 12f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        // Отключаем правую ось
        barChart1.axisRight.isEnabled = false

        val groupSpace = 0.5f
        val barSpace = 0.1f
        data.barWidth = 0.15f

        // Группируем бары
        barChart1.groupBars(0f, groupSpace, barSpace)

        // Настройка границ оси X
        barChart1.xAxis.axisMinimum = 0f
        //val groupCount = xData.size.toFloat()
        //barChart.xAxis.axisMaximum = maxOf(groupCount, barChart.xAxis.axisMaximum)

        // ПРОКРУТКА К ПОСЛЕДНИМ ДАННЫМ (опционально)
        if (xData.size > 12) {
            // Прокручиваем к концу графика
            barChart1.moveViewToX(xData.size.toFloat() - 1)
        }

        // Анимация
        barChart1.animateY(1000)

        // Обновляем график
        barChart1.invalidate()
    }

    private fun initTimeStatistic(statistic: UserStatistic) {

    }

    private fun initRadiusStatistic(statistic: UserStatistic) {
        val barEntriesList = getRadiusList(statistic)

        val barDataSet = BarDataSet(
            barEntriesList,
            "Распределение радиусов"
        ).apply {
            color = Color.BLUE
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        val barData = BarData(barDataSet)
        barChart3.data = barData

        barChart3.apply {
            legend.isEnabled = false
            description.isEnabled = false

            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false

            setExtraOffsets(10f, 30f, 10f, 10f)
        }

        val xLabels = ArrayList<String>()
        xLabels.add("")
        for (i in MIN_RADIUS_KM / 10..MAX_RADIUS_KM / 10) {
            xLabels.add("$i км")
        }

        Log.d(TAG, "Радиусы: $xLabels")

        // Настройка оси X
        val xAxis: XAxis = barChart3.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(xLabels)
            granularity = 1f
            isGranularityEnabled = true
            textSize = 12f
            labelRotationAngle = 30f
            // setCenterAxisLabels(true)
            setAvoidFirstLastClipping(true)
            //setDrawGridLines(false)
            labelCount = xLabels.size

            axisMinimum = (MIN_RADIUS_KM / 10).toFloat() - 1
            axisMaximum = (MAX_RADIUS_KM / 10).toFloat() + 1
        }

        // Настройка левой оси Y
        barChart3.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            setDrawGridLines(true)
            gridLineWidth = 0.5f
            gridColor = Color.LTGRAY
            textSize = 12f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }

            isGranularityEnabled = true
        }

        barChart3.axisRight.isEnabled = false

        barData.barWidth = 0.6f

        barChart3.animateY(800)

        barChart3.invalidate()
    }

    private fun getRadiusList(statistic: UserStatistic): ArrayList<BarEntry> {
        val radiusMap: MutableMap<Int, Int> = LinkedHashMap()

        for (i in (MIN_RADIUS_KM / 10)..(MAX_RADIUS_KM / 10)) {
            radiusMap[i] = 0
        }

        for (route in statistic.routes) {
            route.start.radius?.let { radiusValue ->
                val radiusInTenths = (radiusValue).toInt()

                if (radiusInTenths in (MIN_RADIUS_KM / 10)..(MAX_RADIUS_KM / 10)) {
                    radiusMap[radiusInTenths] = radiusMap[radiusInTenths]?.plus(1) ?: 0
                }
            }
        }

        val barEntriesList = ArrayList<BarEntry>()
        for ((key, value) in radiusMap.entries) {
            barEntriesList.add(
                BarEntry(
                    key.toFloat(),
                    value.toFloat()
                )
            )

            Log.d(TAG, "Радиус ${key / 10.0} км: $value маршрутов")
        }

        return barEntriesList
    }

    private fun getListOfDates(): ArrayList<String> {
        if (periodData.periodType == PeriodType.YEAR) {
            return timeController.getMonths(periodData.dateFrom!!)
        }
        val periodList = arrayListOf(
            timeController.formatDate(periodData.dateFrom.toString()))
        var date: LocalDate = periodData.dateFrom!!
        while (date != periodData.dateTo) {
            date = date.plusDays(1)
            periodList.add(timeController.formatDate(date.toString()))
        }
        return periodList
    }

}