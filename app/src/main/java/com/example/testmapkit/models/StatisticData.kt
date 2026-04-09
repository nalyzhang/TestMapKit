package com.example.testmapkit.models

class StatisticData {
    private lateinit var routes: List<RouteData>
    private var countOfRoutes: Int = 0
    private lateinit var averageTimeOfRoutes: com.yandex.mapkit.Time
}