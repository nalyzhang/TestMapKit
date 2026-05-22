package com.example.testmapkit.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testmapkit.PROCESSING
import com.example.testmapkit.controllers.TimeController

class ChronometerService : Service() {

    private val binder = ChronometerBinder()
    private var elapsedTime = 0L // время в миллисекундах
    private val handler = Handler(Looper.getMainLooper())
    private val _timeLiveData = MutableLiveData<Long>()
    val timeLiveData: LiveData<Long> get() = _timeLiveData
    val timeController = TimeController()

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (PROCESSING) {
                elapsedTime += 1000
                _timeLiveData.postValue(elapsedTime)
                handler.postDelayed(this, 1000)
            }
        }
    }

    inner class ChronometerBinder : Binder() {
        fun getService(): ChronometerService = this@ChronometerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun startChronometer(): String {
        if (!PROCESSING) {
            PROCESSING = true
            elapsedTime = 0
            _timeLiveData.postValue(elapsedTime)
            Log.d("LocationService", "1")
            handler.post(updateRunnable)
        }
        return timeController.formatNow()
    }

    fun stopChronometer(): String {
        PROCESSING = false
        Log.d("LocationService", "0")
        handler.removeCallbacks(updateRunnable)
        return timeController.formatNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}