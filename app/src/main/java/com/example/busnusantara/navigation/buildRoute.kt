package com.example.busnusantara.navigation

import com.google.android.gms.maps.GoogleMap

private const val key = API_KEY

fun buildRoute(
    origin: String,
    dest: String,
    mMap: GoogleMap,
) {
    val strOrigin = "origin=$origin"
    val strDest = "destination=$dest"
    assembleUrlAndGetPath(strOrigin, strDest, mMap)
}

private fun assembleUrlAndGetPath(
    strOrigin: String,
    strDest: String,
    mMap: GoogleMap
) {
    val sensor = "sensor=false"
    val parameters = "$strOrigin&$strDest&$sensor"
    val output = "json"
    val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=$key"
    CalculatePathFromLocation(url) { polyLine -> mMap.addPolyline(polyLine) }.execute()
}