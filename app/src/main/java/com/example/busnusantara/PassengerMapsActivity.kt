package com.example.busnusantara

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityPassengerMapsBinding
import com.example.busnusantara.googleapi.GetPathFromLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PassengerMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerMapsBinding
    private lateinit var driverLocation: GeoPoint

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
        val routeID = "IT5cRoAVJjl16gy0bfGh"
        val startBusStop = LatLng(-6.224854, 106.827)

        getDriverLocation(routeID)
        mMap.addMarker(
            MarkerOptions().position(startBusStop)
                .title("Your nearest bus stop")
        )
    }

    private fun getDriverLocation(routeID: String) {
        /* TODO: Implement */
        val collection = Collections.ROUTES.toString()
        Firebase.firestore.collection(collection)
            .get()
            .addOnSuccessListener { routes ->
                Log.d(ContentValues.TAG, "Finding all $collection with routeID $routeID")
                if (routes.isEmpty || routes.size() > 1) {
                    Log.d(
                        ContentValues.TAG,
                        "routeID $routeID doesn't exist or there are too many such IDs."
                    )
                }

                // Only one routeID, get DocumentReference to find the driver.
                for (route in routes) {
                    Firebase.firestore.collection(Collections.DRIVER.toString())
                        .whereEqualTo("routeId", route.reference)
                        .get().addOnSuccessListener { drivers ->
                            if (drivers.isEmpty) {
                                Log.d(
                                    ContentValues.TAG,
                                    "routeID $routeID doesn't have a driver!."
                                )
                            } else {
                                for (driver in drivers) {
                                    driverLocation = driver.data["location"] as GeoPoint
                                    Log.d(
                                        ContentValues.TAG,
                                        "Updating map to new location of driver: $driverLocation."
                                    )
                                    val driverLoc =
                                        LatLng(driverLocation.latitude, driverLocation.longitude)

                                    // Add a marker for Bus Driver's Location and move the camera
                                    mMap.addMarker(
                                        MarkerOptions().position(driverLoc)
                                            .title("Bus Driver's Location")
                                    )
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLoc))

                                    GetPathFromLocation(
                                        driverLoc, LatLng(-6.224854, 106.827)
                                    ) { polyLine -> mMap.addPolyline(polyLine) }.execute()

                                }
                            }
                        }


                }
            }
    }

    private fun getBusETA() {
        /* TODO: Implement */
    }


}