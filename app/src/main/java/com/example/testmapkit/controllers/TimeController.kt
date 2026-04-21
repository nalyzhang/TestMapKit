package com.example.testmapkit.controllers

import android.util.Log
import com.example.testmapkit.TAG
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.time.LocalDate

class TimeController {

    // Только дата (yyy-MM-DDT00:00:00+00:00 -> dd.MM.yyyy)
    fun extractDate(dateTimeString: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(dateTimeString)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            offsetDateTime.format(formatter)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    // Только время (yyy-MM-DDT00:00:00+00:00 -> HH:mm:ss)
    fun extractTime(dateTimeString: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(dateTimeString)
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            offsetDateTime.format(formatter)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    // Полная дата и время
    // yyy-MM-DDT00:00:00+00:00 -> dd.MM.yyyy HH:mm:ss
    fun extractDateTime(dateTimeString: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(dateTimeString)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            offsetDateTime.format(formatter)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    fun formatNow(): String {
        return try {
            val now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            now.format(formatter)
        } catch (e: Exception) {
            // Запасной вариант с текущим временем без часового пояса
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            "${now.format(formatter)}+03:00"
        }
    }

    // yyy-MM-DD -> DD.MM.yyyy
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    // DD.MM.yyyy -> yyy-MM-DD
    fun formatDateReverse(dateString: String): String {
        return try {
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun getTimeDifference(startDateTime: String, endDateTime: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd'T'HH:mm:ssXXX")
            val start = ZonedDateTime.parse(startDateTime, formatter)
            val end = ZonedDateTime.parse(endDateTime, formatter)
            Log.d(TAG, "$start, $end")

            val duration = end.toEpochSecond() - start.toEpochSecond()
            Log.d(TAG, "$duration")

            formatTimeFromSeconds(duration)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    fun formatTimeFromSeconds(timeInSeconds: Long): String {
        val hours = (timeInSeconds / 3600).toInt()
        val minutes = ((timeInSeconds % 3600) / 60).toInt()
        val seconds = (timeInSeconds % 60).toInt()

        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours, minutes, seconds)
    }

    fun formatMonth(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(
            "MMM yyyy", Locale.getDefault()
        ))
    }

    fun formatYear(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(
            "yyyy", Locale.getDefault()
        ))
    }

}