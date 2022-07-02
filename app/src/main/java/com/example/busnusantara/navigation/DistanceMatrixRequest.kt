package com.example.busnusantara.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DistanceMatrixRequest {
    private var client = OkHttpClient()

    /**
     * Call Google Maps API to find the distance matrix from bus location to passenger bus stop.
     */
    @Throws(IOException::class)
    fun run(start: GeoPoint, destination: GeoPoint): String {
        val origins = "${start.latitude},${start.longitude}"
        val destinationCoords = "${destination.latitude},${destination.longitude}"
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origins&" +
                "destinations=$destinationCoords&language=en-EN&key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    fun getDistanceAndDuration(start: GeoPoint, end: GeoPoint): Pair<String, String> {
        val request = DistanceMatrixRequest()
        val response = request.run(start, end)
        val gson = Gson()
        val directionResponse: DirectionResponse =
            gson.fromJson(response, DirectionResponse::class.java)
        val directionResponseElements = directionResponse.rows[0].elements[0]
        val distanceText = directionResponseElements.distance.text
        val durationText = directionResponseElements.duration.text
        return Pair(distanceText, durationText)
    }

    private fun sumDistanceAndDurationStrings(
        prevDurDist: Pair<String, String>,
        nextDurDist: Pair<String, String>? = null
    ): Pair<String, String> {

        if (nextDurDist == null) return prevDurDist

        val prevDist = prevDurDist.first.split(" ")[0].toDouble()
        val nextDist = nextDurDist.first.split(" ")[0].toDouble()

        // Get Time from bus to previous stop.
        val prevDurations = (prevDurDist.second.split(" "))


        // No hours, only minutes
        var prevHours = 0.0
        var prevMinutes = prevDurations[0].toDouble()
        if (prevDurations.size >= 3) {
            prevHours = prevDurations[0].toDouble()
            prevMinutes = prevDurations[2].toDouble()
        }
        val prevTime = prevHours * MINUTES_IN_HOUR + prevMinutes

        // Get Time from previous stop to next stop.
        val nextDurations = (nextDurDist.second.split(" "))

        // No hours, only minutes
        var nextHours = 0.0
        var nextMinutes = prevDurations[0].toDouble()
        if (nextDurations.size >= 3) {
            nextHours = nextDurations[0].toDouble()
            nextMinutes = nextDurations[2].toDouble()
        }

        val nextTime = nextHours * MINUTES_IN_HOUR + nextMinutes + prevTime
        val (hrs, mins) = Pair(nextTime.toInt() / MINUTES_IN_HOUR, nextTime % MINUTES_IN_HOUR)


        return Pair("${(prevDist + nextDist)} km", "$hrs hours $mins minutes")
    }

    private fun getAllDistanceAndDurations(stops: List<GeoPoint>): MutableList<Pair<String, String>> {
        val stopDistanceDurations = mutableListOf<Pair<String, String>>()
        val summedStopDistanceDurations: MutableList<Pair<String, String>> = mutableListOf()
        for (i in 1 until stops.size) {
            val prevDurDist = DistanceMatrixRequest().getDistanceAndDuration(stops[i - 1], stops[i])
            stopDistanceDurations.add(sumDistanceAndDurationStrings(prevDurDist))
        }

        Log.d("Distance", "stop distance durations:\n$stopDistanceDurations")
        summedStopDistanceDurations.add(stopDistanceDurations[0])
        for (i in 1 until stopDistanceDurations.size) {
            summedStopDistanceDurations.add(
                sumDistanceAndDurationStrings(
                    summedStopDistanceDurations[i - 1],
                    stopDistanceDurations[i]
                )
            )
        }
        return summedStopDistanceDurations
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertFromTimeLengthsToETAs(stops: List<GeoPoint>): MutableList<String> {
        val stopDurations = getAllDistanceAndDurations(stops)
        val stopETAs = mutableListOf<String>()
        val myTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val df = SimpleDateFormat("HH:mm", Locale.UK)
        val d = df.parse(myTime)
        val cal = Calendar.getInstance()

        stopDurations.forEach { (_, time) ->
            val tokens = time.split(" ")
            var hrs = 0
            var mins = tokens[0].toInt()
            if (tokens.size >= 3) {
                hrs = mins
                mins = tokens[2].toDouble().toInt()
            }

            if (d != null) {
                cal.time = d
            }
            cal.add(Calendar.HOUR, hrs)
            cal.add(Calendar.MINUTE, mins)
            stopETAs.add(df.format(cal.time))
        }

        return stopETAs
    }

    private class DirectionResponse(
        val destination_addresses: Array<String>,
        val origin_addresses: Array<String>,
        val rows: ArrayList<DirectionResponseRows>
    )

    private class DirectionResponseRows(
        val elements: List<Elements>
    )

    private class Elements(
        val distance: Element,
        val duration: Element
    )

    private class Element(
        val text: String,
        val value: String
    )

    companion object {
        const val MINUTES_IN_HOUR = 60
    }
}
