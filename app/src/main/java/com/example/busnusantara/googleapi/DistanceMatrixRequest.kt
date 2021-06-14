package com.example.busnusantara.googleapi

import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
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
    fun run(start: GeoPoint, destination: GeoPoint): String {
        val origins = "${start.latitude},${start.longitude}"
        val destination = "${destination.latitude},${destination.longitude}"
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origins&" +
                    "destinations=$destination&language=en-EN&key=$KEY"
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    fun getDistanceAndDuration(start: GeoPoint, end: GeoPoint): Pair<String, String> {
        val request = DistanceMatrixRequest()
        val response = request.run(GeoPoint(-7.5, 110.23), GeoPoint(-7.9, 111.23))
//        val response = request.run(start, end)
        val gson = Gson()

        val directionResponse: DirectionResponse =
            gson.fromJson(response, DirectionResponse::class.java)
        var directionResponseElements = directionResponse.rows[0].elements[0]
        val distanceText = directionResponseElements.distance.text
        val durationText = directionResponseElements.duration.text
        return Pair(distanceText, durationText)
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
        private const val KEY = API_KEY
        val resp = """
            {
               "destination_addresses" : [ "Jakarta, Indonesia" ],
               "origin_addresses" : [
                  "Jl. Tidar Sawe, Tidar Sel., Kec. Magelang Sel., Kota Magelang, Jawa Tengah 56125, Indonesia"
               ],
               "rows" : [
                  {
                     "elements" : [
                        {
                           "distance" : {
                              "text" : "514 km",
                              "value" : 513827
                           },
                           "duration" : {
                              "text" : "7 hours 2 mins",
                              "value" : 25326
                           },
                           "status" : "OK"
                        }
                     ]
                  }
               ],
               "status" : "OK"
            }
        """.trimIndent()

        /* *
            For testing purposes only
         */
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val (distance, duration) = DistanceMatrixRequest().getDistanceAndDuration(
                GeoPoint(-7.5, 110.23),
                GeoPoint(-7.9, 111.23)
            )
            println("> From JSON String - distance:\n${distance}")
            println("> From JSON String - duration:\n${duration}")
        }
    }
}
