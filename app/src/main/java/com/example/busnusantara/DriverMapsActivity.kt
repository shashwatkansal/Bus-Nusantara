package com.example.busnusantara

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.example.busnusantara.database.Collections

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.busnusantara.databinding.ActivityMapsBinding
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val startLocation = "Jakarta"
        val destination = "Yogyakarta"

        addJourneyStops(startLocation, destination)

        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)
    }

    private fun addJourneyStops(start: String, destination: String) {
        Firebase.firestore.collection(Collections.ROUTES.toString())
            .whereEqualTo("destination", destination)
            .get().addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding all routes with $destination dest")
                if (documents.isEmpty) {
                    textView.text = "No route was found. Please try again"
                }
                for (document in documents) {
                    val stopsData = document.data.get("stops")
                    if (stopsData is List<*>) {
                        var stops: List<String> = stopsData.filterIsInstance<String>()
                        stops = listOf(start) + stops + destination
                        for (stop in stops) {
                            addStopOnMap(stop)
                        }
                    }
                }
            }
    }

    private fun addStopOnMap(stop: String) {
        Firebase.firestore.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get().addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding stop $stop agent")
                if (documents.isEmpty) {
                    textView.text = "No agent was found. Please try again"
                }
                for (document in documents) {
                    val coordinate: GeoPoint? = document.getGeoPoint("coordinate")
                    if (coordinate != null) {
                        val lat = coordinate?.getLatitude()
                        val lng = coordinate?.getLongitude()
                        val agent = LatLng(lat, lng)

                        mMap.addMarker(MarkerOptions().position(agent).title(stop))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(agent))
                    }
                }
            }
    }
}