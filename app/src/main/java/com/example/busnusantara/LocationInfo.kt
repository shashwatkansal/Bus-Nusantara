package com.example.busnusantara

import java.util.*

data class LocationInfo(
    val locationName: String,
    var passengerCount: Int,
    var eta: Date
)