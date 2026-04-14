package com.example.testmapkit.fragments.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testmapkit.TAG
import com.example.testmapkit.models.LocationData

class SharedLocationsViewModel: ViewModel() {
    private val _startLocation = MutableLiveData<LocationData?>()
    val startLocation: LiveData<LocationData?> = _startLocation

    private val _finishLocation = MutableLiveData<LocationData?>()
    val finishLocation: LiveData<LocationData?> = _finishLocation

    fun setStartLocation(location: LocationData?) {
        Log.d(TAG, "Локация сохранена ${location?.getAddress()}")
        _startLocation.value = location
    }

    fun setFinishLocation(location: LocationData?) {
        Log.d(TAG, "Локация сохранена ${location?.getAddress()}")
        _finishLocation.value = location
    }

    fun getStartLocationValue(): LocationData? {
        Log.d(TAG, "getStartLocationValue: ${_startLocation.value?.getAddress()}")
        return _startLocation.value
    }

    fun getFinishLocationValue(): LocationData? {
        Log.d(TAG, "getFinishLocationValue: ${_finishLocation.value?.getAddress()}")
        return _finishLocation.value
    }
}