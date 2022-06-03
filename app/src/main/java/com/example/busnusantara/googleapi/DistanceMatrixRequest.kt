package com.example.busnusantara.googleapi

import android.os.Build
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
     * Call Google Maps API to find the distance matrix
     * from bus location to passenger bus stop.
     */
    @Throws(IOException::class)
    fun run(start: GeoPoint, destination: GeoPoint): String {
        val origins = "${start.latitude},${start.longitude}"
        val destination = "${destination.latitude},${destination.longitude}"
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origins&" +
                    "destinations=$destination&language=en-EN&key=$API_KEY"
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

        val MINS_IN_HOUR = 60

        if (nextDurDist == null) {
            return prevDurDist
        }

        val prevDist = (prevDurDist.first.split(" ")[0]).toDouble()
        val nextDist = (nextDurDist.first.split(" ")[0]).toDouble()

        // Get Time from bus to previous stop.
        val prevDurations = (prevDurDist.second.split(" "))


        // No hours, only minutes
        var prevHrs = 0.0
        var prevMins = prevDurations[0].toDouble()
        if (prevDurations.size >= 3) {
            prevHrs = prevDurations[0].toDouble()
            prevMins = prevDurations[2].toDouble()
        }
        val prevTime = prevHrs * MINS_IN_HOUR + prevMins

        // Get Time from previous stop to next stop.
        val nextDurations = (nextDurDist.second.split(" "))

        // No hours, only minutes
        var nextHours = 0.0
        var nextMinutes = prevDurations[0].toDouble()
        if (nextDurations.size >= 3) {
            nextHours = nextDurations[0].toDouble()
            nextMinutes = nextDurations[2].toDouble()
        }
        val nextTime = nextHours * MINS_IN_HOUR + nextMinutes + prevTime

        val (hrs, mins) = Pair(nextTime.toInt() / MINS_IN_HOUR, nextTime % MINS_IN_HOUR)


        return Pair("${(prevDist + nextDist)} km", "$hrs hours $mins mins")
    }

    private fun getAllDistanceAndDurations(stops: List<GeoPoint>): MutableList<Pair<String, String>> {
        val stopDistanceDurations: MutableList<Pair<String, String>> = mutableListOf()
        val summedStopDistanceDurations: MutableList<Pair<String, String>> = mutableListOf()
        for (i in 1 until stops.size) {
            val prevDurDist = DistanceMatrixRequest().getDistanceAndDuration(
                stops[i - 1],
                stops[i]
            )
            stopDistanceDurations.add(sumDistanceAndDurationStrings(prevDurDist))
        }
//        println("stop distance durations:\n$stopDistanceDurations")
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
        val stopDurations: MutableList<Pair<String, String>> = getAllDistanceAndDurations(stops)
        val stopETAs: MutableList<String> = mutableListOf()
        val myTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val df = SimpleDateFormat("HH:mm")
        val d = df.parse(myTime)
        val cal = Calendar.getInstance()

        for ((_, time) in stopDurations) {
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
}
