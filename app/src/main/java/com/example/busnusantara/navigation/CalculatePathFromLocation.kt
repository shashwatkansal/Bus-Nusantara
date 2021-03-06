package com.example.busnusantara.navigation

import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class CalculatePathFromLocation(
    private val url: String,
    private val resultCallback: DirectionPointListener
) : AsyncTask<String?, Void?, PolylineOptions?>() {

    private val tag = "GetPathFromLocation"

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String?): PolylineOptions? {
        val data: String
        return try {
            var inputStream: InputStream? = null
            var connection: HttpURLConnection? = null

            try {
                val directionUrl = URL(url)
                connection = directionUrl.openConnection() as HttpURLConnection
                connection.connect()
                inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuffer = StringBuffer()
                var line: String? = ""
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuffer.append(line)
                }
                data = stringBuffer.toString()
                bufferedReader.close()
            } catch (e: Exception) {
                Log.e(tag, "Exception : $e")
                return null
            } finally {
                inputStream!!.close()
                connection!!.disconnect()
            }

            Log.e(tag, "Background Task data : $data")

            val jsonObject: JSONObject
            val routes: List<List<HashMap<String?, String>>>?
            try {
                jsonObject = JSONObject(data)

                // Starts parsing data
                val helper = ParseDirectionData()
                routes = helper.parse(jsonObject)

                Log.e(tag, "Executing Routes : " /*, routes.toString()*/)
                var points: ArrayList<LatLng?>
                var lineOptions: PolylineOptions? = null

                // Traversing through all the routes
                routes.indices.forEach { i ->
                    points = ArrayList()
                    lineOptions = PolylineOptions()

                    // Fetching i-th route
                    val path = routes[i]

                    // Fetching all the points in i-th route
                    for (j in path.indices) {
                        val point = path[j]
                        val lat = point["lat"]!!.toDouble()
                        val lng = point["lng"]!!.toDouble()
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions!!.addAll(points)
                    lineOptions!!.width(10f)
                    lineOptions!!.color(Color.BLUE)
                    Log.e(tag, "PolylineOptions Decoded")
                }

                // Drawing polyline in the Google Map for the i-th route
                lineOptions

            } catch (e: Exception) {
                Log.e(tag, "Exception in Executing Routes : $e")
                null
            }

        } catch (e: Exception) {
            Log.e(tag, "Background Task Exception : $e")
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(polylineOptions: PolylineOptions?) {
        super.onPostExecute(polylineOptions)
        if (polylineOptions != null) {
            resultCallback.onPath(polylineOptions)
        }
    }
}