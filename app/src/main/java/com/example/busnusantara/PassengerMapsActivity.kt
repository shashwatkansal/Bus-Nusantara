package com.example.busnusantara

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityPassengerMapsBinding
import com.example.busnusantara.googleapi.buildRoute
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PassengerMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPassengerMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val orderID = "oolO6KVivO3Z445xu5cW"

        getPassengerBusStopAndMark(orderID)
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)

    }

    private fun getPassengerBusStopAndMark(orderId: String) {
        Firebase.firestore.collection(Collections.ORDERS.toString())
            .document(orderId).get().addOnSuccessListener { order ->
                if (order != null) {
                    val pickupLocation = order["pickupLocation"] as String
                    val tripDocRef = order["tripID"] as DocumentReference
                    val tripRef = tripDocRef.id.toString()
                    Log.d(TAG, "tripRef is $tripRef")
                    Log.d(TAG, "pickupLocation is $pickupLocation")

                    // Mark the passenger's location on map
                    Firebase.firestore.collection(Collections.AGENTS.toString())
                        .whereEqualTo("locationBased", pickupLocation)
                        .get().addOnSuccessListener { agents ->
                            if (!agents.isEmpty) {
                                for (agent in agents) {
                                    Log.d(TAG, "Getting Agent $agent")
                                    val passengerLoc = agent?.getGeoPoint("coordinate")?.let {
                                        geoPointToLatLng(
                                            it
                                        )
                                    }
                                    mMap.addMarker(
                                        MarkerOptions().position(passengerLoc)
                                            .title("Your stop").icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_ORANGE
                                                )
                                            )
                                    )
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLoc))
                                }
                            }
                        }

                    /* Get the driver's location and mark it */
                    Firebase.firestore.collection(Collections.TRIPS.toString())
                        .document(tripRef).get().addOnSuccessListener { trip ->
                            Log.d(TAG, "Getting trip $trip")
                            if (trip != null) {
                                val incomingDriverLocation = trip.data?.get("location") as GeoPoint

                                mMap.addMarker(
                                    MarkerOptions()
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_ORANGE
                                            )
                                        )
                                        .position(geoPointToLatLng(incomingDriverLocation))
                                        .title("Bus Driver")
                                )

                                // Mark all the stops on map
                                val routeDocRef = trip.data!!["routeID"] as DocumentReference
                                val routeID = routeDocRef.id
                                Firebase.firestore.collection(Collections.ROUTES.toString())
                                    .document(routeID).get().addOnSuccessListener { route ->
                                        Log.d(TAG, "Getting route $route")
                                        if (route != null) {
                                            var start = route.get("start") as String
                                            Log.d(TAG, "Found start location $start")
                                            val stops = route.get("stops") as ArrayList<String>
                                            Log.d(TAG, "Found stops locations $stops")
                                            addStopOnMap(start)

                                            for (stop in stops) {
                                                Log.d(TAG, "Adding stop $stop on map")
                                                addStopOnMap(stop)
                                                buildRoute(start, stop, mMap)
                                                start = stop
                                            }
                                            buildRoute(
                                                geoPointToLatLng(incomingDriverLocation),
                                                start,
                                                mMap
                                            )
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun getBusETA() {
        /* TODO: Implement */
    }

    private fun addStopOnMap(stop: String) {
        Firebase.firestore.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get().addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding stop $stop agent")
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val coordinate: GeoPoint? = document.getGeoPoint("coordinate")
                        if (coordinate != null) {
                            val lat = coordinate.latitude
                            val lng = coordinate.longitude
                            val agent = LatLng(lat, lng)

                            mMap.addMarker(MarkerOptions().position(agent).title(stop))
                        }
                    }
                }
            }
    }

    private fun geoPointToLatLng(geoPoint: GeoPoint): LatLng {
        return LatLng(geoPoint.latitude, geoPoint.longitude)
    }


}