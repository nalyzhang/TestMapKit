package com.example.testmapkit.models

import java.time.LocalDate
import java.time.temporal.ChronoField

enum class PeriodType {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

class PeriodData(
    var dateFrom: LocalDate?,
    var dateTo: LocalDate?,
    var periodType: PeriodType?
) {

    fun isLeapYear(date: LocalDate): Boolean {
        val year = date.year
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun setDay(date: LocalDate) {
        dateFrom = date
        dateTo = date
        periodType = PeriodType.DAY
    }

    fun setWeek(date: LocalDate) {
        val monday = date.with(ChronoField.DAY_OF_WEEK, 1)
        val sunday = date.with(ChronoField.DAY_OF_WEEK, 7)

        dateFrom = monday
        dateTo = sunday
        periodType = PeriodType.WEEK
    }

    fun setMonth(date: LocalDate) {
        val firstDay = date.with(ChronoField.DAY_OF_MONTH, 1)
        val monthDay = date.monthValue
        val lastDay: LocalDate
        if (monthDay in listOf(1,3,5,7,8,10,12))
            lastDay = date.with(ChronoField.DAY_OF_MONTH, 31)
        else if (monthDay in listOf(4,6,9,11))
            lastDay = date.with(ChronoField.DAY_OF_MONTH, 30)
        else {
            if (isLeapYear(date))
                lastDay = date.with(ChronoField.DAY_OF_MONTH, 29)
            else
                lastDay = date.with(ChronoField.DAY_OF_MONTH, 28)
        }

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.MONTH
    }

    fun setYear(date: LocalDate) {
        val firstDay = date.with(ChronoField.DAY_OF_YEAR, 1)
        val lastDay: LocalDate
        if (isLeapYear(date))
            lastDay = date.with(ChronoField.DAY_OF_YEAR, 366)
        else
            lastDay = date.with(ChronoField.DAY_OF_YEAR, 365)

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.YEAR
    }

    fun previousDay(date: LocalDate) {
        dateFrom = date.minusDays(1)
        dateTo = date.minusDays(1)
        periodType = PeriodType.DAY
    }

    fun previousWeek(date: LocalDate) {
        val monday = date.minusWeeks(
            1).with(ChronoField.DAY_OF_WEEK, 1)
        val sunday = date.minusWeeks(
            1).with(ChronoField.DAY_OF_WEEK, 7)

        dateFrom = monday
        dateTo = sunday
        periodType = PeriodType.WEEK
    }

    fun previousMonth(date: LocalDate) {
        val firstDay = date.minusMonths(
            1).with(ChronoField.DAY_OF_MONTH, 1)
        val monthDay = firstDay.monthValue
        val lastDay: LocalDate
        if (monthDay in listOf(1,3,5,7,8,10,12))
            lastDay = date.minusMonths(
                1).with(ChronoField.DAY_OF_MONTH, 31)
        else if (monthDay in listOf(4,6,9,11))
            lastDay = date.minusMonths(
                1).with(ChronoField.DAY_OF_MONTH, 30)
        else {
            if (isLeapYear(date))
                lastDay = date.minusMonths(
                    1).with(ChronoField.DAY_OF_MONTH, 29)
            else
                lastDay = date.minusMonths(
                    1).with(ChronoField.DAY_OF_MONTH, 28)
        }

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.MONTH
    }

    fun previousYear(date: LocalDate) {
        val firstDay = date.minusYears(
            1).with(ChronoField.DAY_OF_YEAR, 1)
        val lastDay: LocalDate
        if (isLeapYear(date))
            lastDay = date.minusYears(
                1).with(ChronoField.DAY_OF_YEAR, 366)
        else
            lastDay = date.minusYears(
                1).with(ChronoField.DAY_OF_YEAR, 365)

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.YEAR
    }

    fun nextDay(date: LocalDate) {
        dateFrom = date.plusDays(1)
        dateTo = date.plusDays(1)
        periodType = PeriodType.DAY
    }

    fun nextWeek(date: LocalDate) {
        val monday = date.plusWeeks(
            1).with(ChronoField.DAY_OF_WEEK, 1)
        val sunday = date.plusWeeks(
            1).with(ChronoField.DAY_OF_WEEK, 7)

        dateFrom = monday
        dateTo = sunday
        periodType = PeriodType.WEEK
    }

    fun nextMonth(date: LocalDate) {
        val firstDay = date.plusMonths(
            1).with(ChronoField.DAY_OF_MONTH, 1)
        val monthDay = firstDay.monthValue
        val lastDay: LocalDate
        if (monthDay in listOf(1,3,5,7,8,10,12))
            lastDay = date.plusMonths(
                1).with(ChronoField.DAY_OF_MONTH, 31)
        else if (monthDay in listOf(4,6,9,11))
            lastDay = date.plusMonths(
                1).with(ChronoField.DAY_OF_MONTH, 30)
        else {
            if (isLeapYear(date))
                lastDay = date.plusMonths(
                    1).with(ChronoField.DAY_OF_MONTH, 29)
            else
                lastDay = date.plusMonths(
                    1).with(ChronoField.DAY_OF_MONTH, 28)
        }

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.MONTH
    }

    fun nextYear(date: LocalDate) {
        val firstDay = date.plusYears(
            1).with(ChronoField.DAY_OF_YEAR, 1)
        val lastDay: LocalDate
        if (isLeapYear(date))
            lastDay = date.plusYears(
                1).with(ChronoField.DAY_OF_YEAR, 366)
        else
            lastDay = date.plusYears(
                1).with(ChronoField.DAY_OF_YEAR, 365)

        dateFrom = firstDay
        dateTo = lastDay
        periodType = PeriodType.YEAR
    }
}