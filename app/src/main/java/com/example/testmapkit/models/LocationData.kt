package com.example.testmapkit.models

import java.io.Serializable

class LocationData(
    val longitude: Double,
    val latitude: Double,
    val circleRadius: Double? = null
) : Serializable {

    private var address: String = ""
    private var dateTime: String = ""
    private var idServer: Int = 0

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

    fun getStringRadius(): String{
        return "$circleRadius км"
    }

    fun getStringAddress(): String {
        val list = address.split(' ')
        var textAddress: String = ""
        for (i in 1..<list.size-1) {
            textAddress += "${list[i]} "
        }
        textAddress += list[list.size-1]
        return textAddress
    }

    fun getTown(): String {
        val list = address.split(',')
        return list[0]
    }

    fun setID(id: Int) {
        idServer = id
    }

    fun getID(): Int {
        return idServer
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}