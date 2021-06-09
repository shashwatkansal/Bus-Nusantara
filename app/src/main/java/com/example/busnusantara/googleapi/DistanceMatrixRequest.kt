package com.example.busnusantara.googleapi

import com.google.firebase.firestore.GeoPoint
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.IOException

class DistanceMatrixRequest {
    var client = OkHttpClient()

    /**
     * Call Google Maps API to find the distance matrix
     * from bus location to passenger bus stop.
     */
    @Throws(IOException::class)
    fun run(start: GeoPoint, destination: String): String {
        val origins = "${start.latitude},${start.longitude}"
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origins&" +
                    "destinations=$destination&language=en-EN&key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    companion object {
        private const val API_KEY = "AIzaSyD1zYs0nA_Et9Yj11ICVbDeMO-jczVu3hU"

        /* *
            For testing purposes only
         */
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val request = DistanceMatrixRequest()

            val response = request.run(GeoPoint(-7.5, 110.23), "Jakarta")
            println(response)
        }
    }
}