package com.example.testmapkit.models

import android.location.Address
import com.example.testmapkit.controllers.SearchController


class LocationData(
    val longitude: Double,
    val latitude: Double,
    val circleRadius: Int
) {

    private lateinit var address: Address

    fun setAddress(textAddresses: Address?) {
        if (textAddresses != null) address = textAddresses
    }

    fun setAddress () {
        val result = SearchController().getAddress(longitude, latitude)
        if (result != null) address = result
    }

    fun getAddress(): Address {
        return address
    }

}