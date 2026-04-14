package com.example.testmapkit.models

import java.io.Serializable

class LocationData(
    val longitude: Double,
    val latitude: Double,
    val circleRadius: Double? = null
) : Serializable {

    private var address: String = ""
    private var dateTime: String = ""

    fun setAddress(textAddresses: String?) {
        if (textAddresses != null) address = textAddresses
    }

    fun getAddress(): String {
        return address
    }

    fun setDateTime(textDateTime: String) {
        dateTime = textDateTime
    }

    fun getDateTime(): String {
        return dateTime
    }

    override fun toString(): String {
        return "LocationData(lat=$latitude, lon=$longitude, radius=$circleRadius, address='$address')"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}