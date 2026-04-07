package com.example.testmapkit.controllers

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class TimeController {

    // Только дата (yyy-MM-DDT00:00:00+00:00 -> yyyy-MM-dd)
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

}