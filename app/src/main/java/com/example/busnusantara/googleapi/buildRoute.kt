package com.example.busnusantara.googleapi

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

/* Please enter API KEY */
private const val key = API_KEY

fun buildRoute(
    origin: LatLng,
    dest: LatLng,
    mMap: GoogleMap,
) {
    val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
    val strDest = "destination=" + dest.latitude + "," + dest.longitude
    assembleUrlAndGetPath(strOrigin, strDest, mMap)
}

fun buildRoute(
    origin: LatLng,
    dest: String,
    mMap: GoogleMap,
) {
    val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
    val strDest = "destination=$dest"
    assembleUrlAndGetPath(strOrigin, strDest, mMap)
}

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