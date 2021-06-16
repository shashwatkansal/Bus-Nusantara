package com.example.busnusantara

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityPassengerMapsBinding
import com.example.busnusantara.googleapi.buildRoute
import com.example.busnusantara.googleapi.DistanceMatrixRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_driver_maps.*
import kotlinx.android.synthetic.main.activity_driver_maps.bottomSheet
import kotlinx.android.synthetic.main.activity_driver_maps.rvLocations
import kotlinx.android.synthetic.main.activity_passenger_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PassengerMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerMapsBinding

    private lateinit var orderId: String
    private lateinit var tripRef: DocumentReference

    private val distanceMatrixRequest = DistanceMatrixRequest()
    private var passengerLoc: LatLng? = null
    private var stopRequested: Boolean = false


    private lateinit var locationInfoAdapter: LocationInfoAdapter
    private val routeStops: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPassengerMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get order details
        orderId = intent.getStringExtra("ORDER_ID") ?: ""
        Firebase.firestore.document(orderId).get()
            .addOnSuccessListener { order ->
                if (order != null) {
                    tripRef = order["tripID"] as DocumentReference
                    stopRequested = order["stopRequested"] as Boolean
                    if (stopRequested) {
                        toggleRequestButton(false)
                    }
                    Log.d(TAG, "tripRef is $tripRef")

                    setImpromptuStopInfo()
                    requestStopButton.setOnClickListener { _ -> toggleRequestButton(true) }
                }
            }
            .addOnFailureListener {
                TODO("add failure warning")
            }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleRequestButton(updateValue: Boolean) {
        if (stopRequested) {
            requestStopButton.backgroundTintList = resources.getColorStateList(R.color.softblue)
            requestStopButton.text = resources.getString(R.string.request_stop)
            requestStopButton.tooltipText = "@string/stop_request_tooltip"
            if (updateValue) {
                tripRef.update("breakRequests", FieldValue.increment(-1))
            }
        } else {
            requestStopButton.backgroundTintList = resources.getColorStateList(R.color.light_orange)
            requestStopButton.text = resources.getString(R.string.requested)
            requestStopButton.tooltipText = "@string/requested_tooltip"
            if (updateValue) {
                tripRef.update("breakRequests", FieldValue.increment(1))
            }
        }
        stopRequested = !stopRequested
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getPassengerBusStopAndMark()
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)

    }

    private fun getPassengerBusStopAndMark() {
        Firebase.firestore.document(orderId).get()
            .addOnSuccessListener { order ->
                if (order != null) {
                    val pickupLocation = order["pickupLocation"] as String
                    tripRef = order["tripID"] as DocumentReference
                    Log.d(TAG, "tripRef is $tripRef")
                    Log.d(TAG, "pickupLocation is $pickupLocation")

                    // Mark the passenger's location on map
                    markPassengerLocationOnMap(pickupLocation)

                    /* Get the driver's location and mark it */
                    tripRef.get().addOnSuccessListener { trip ->
                        Log.d(TAG, "Getting trip $trip")
                        if (trip != null) {
                            val incomingDriverLocation = trip.data?.get("location") as GeoPoint
                            val incomingDriverLatLng = geoPointToLatLng(incomingDriverLocation)

                            var currPassengerLoc = passengerLoc
                            CoroutineScope(IO).launch {
                                while (currPassengerLoc == null) {
                                    currPassengerLoc = passengerLoc
                                }
                                getETA(
                                    incomingDriverLocation,
                                    GeoPoint(
                                        currPassengerLoc!!.latitude,
                                        currPassengerLoc!!.longitude
                                    )
                                )
                            }
                            addMarkerOnMap(
                                "Bus Driver",
                                incomingDriverLatLng,
                                BitmapDescriptorFactory.HUE_AZURE
                            )
                            mMap.animateCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(incomingDriverLatLng, 10.0f)
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
                                        routeStops.add(start)

                                        for (stop in stops) {
                                            Log.d(TAG, "Adding stop $stop on map")
                                            addStopOnMap(stop)
                                            routeStops.add(stop)
                                            buildRoute(start, stop, mMap)
                                            start = stop
                                        }

                                        routeStops.add(route.get("destination") as String)
                                        setupBottomSheet()
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

    private fun addMarkerOnMap(
        title: String,
        incomingDriverLatLng: LatLng,
        color: Float = BitmapDescriptorFactory.HUE_RED
    ) {
        mMap.addMarker(
            MarkerOptions()
                .position(incomingDriverLatLng)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        color
                    )
                )
                .title(title)
        )
    }

    // Mark Passenger Location On Map
    private fun PassengerMapsActivity.markPassengerLocationOnMap(pickupLocation: String) {
        Firebase.firestore.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", pickupLocation)
            .get().addOnSuccessListener { agents ->
                if (!agents.isEmpty) {
                    for (agent in agents) {
                        Log.d(TAG, "Getting Agent $agent")
                        passengerLoc = agent?.getGeoPoint("coordinate")?.let {
                            geoPointToLatLng(
                                it
                            )
                        }
                        if (passengerLoc != null) {
                            addMarkerOnMap(
                                "Your Start",
                                passengerLoc!!,
                                BitmapDescriptorFactory.HUE_ORANGE
                            )
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLoc))
                        }
                    }
                }
            }
    }

    private fun addStopOnMap(stop: String) {
        Firebase.firestore.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get().addOnSuccessListener { documents ->
                Log.d(TAG, "Finding stop $stop agent")
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val coordinate: GeoPoint? = document.getGeoPoint("coordinate")
                        if (coordinate != null) {
                            val lat = coordinate.latitude
                            val lng = coordinate.longitude
                            val agent = LatLng(lat, lng)
                            addMarkerOnMap(stop, agent)
                        }
                    }
                }
            }
    }

    private fun geoPointToLatLng(geoPoint: GeoPoint): LatLng {
        return LatLng(geoPoint.latitude, geoPoint.longitude)
    }

    private fun setupBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).peekHeight = 300
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED

        locationInfoAdapter = LocationInfoAdapter(routeStops.map { stop ->
            LocationInfo(stop, 3)
        })
        rvLocations.adapter = locationInfoAdapter
        rvLocations.layoutManager = LinearLayoutManager(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setImpromptuStopInfo() {
        // add listener to changes on trip to update impromptu stop info
        tripRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val trip = snapshot.data
                val stoppingSoon = trip?.get("impromptuStop") as Boolean
                if (stoppingSoon) {
                    stopSoonText.text = "Bus stopping soon"
                } else {
                    stopSoonText.text = "Bus not stopping soon"
                }

                val newLocation = trip?.get("location") as GeoPoint
                val currPassengerLoc = passengerLoc
                if (currPassengerLoc != null) {

                    CoroutineScope(IO).launch {
                        getETA(
                            newLocation,
                            GeoPoint(currPassengerLoc.latitude, currPassengerLoc.longitude)
                        )
                    }
                }
            } else {
                Log.d("Impromptu stop", "Failed in getting trip data on listener")
            }
        }
    }

    private fun updateETA(distance: String, duration: String) {
        busDurationValue.text = duration
    }

    private suspend fun getETA(newLocation: GeoPoint, currPassengerLoc: GeoPoint) {
        val (distance, duration) = distanceMatrixRequest
            .getDistanceAndDuration(
                newLocation,
                currPassengerLoc
            )

        withContext(Main) {
            updateETA(distance, duration)
        }
    }

}