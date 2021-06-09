package com.example.busnusantara.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class TrackingService : Service() {

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationCallback: LocationCallback? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.d(
                    "EZRA",
                    "Lat is: ${p0.lastLocation.latitude}, Lon is ${p0.lastLocation.longitude}"
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLocation()
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val locRequest = LocationRequest.create().apply {
            interval = 2000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
}