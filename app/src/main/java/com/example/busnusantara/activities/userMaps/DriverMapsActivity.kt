package com.example.busnusantara.activities.userMaps

import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.busnusantara.R
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityDriverMapsBinding
import com.example.busnusantara.location.LocationInfo
import com.example.busnusantara.location.LocationInfoAdapter
import com.example.busnusantara.navigation.DistanceMatrixRequest
import com.example.busnusantara.navigation.buildRoute
import com.example.busnusantara.services.TrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.from
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_driver_maps.*
import kotlinx.android.synthetic.main.location_info.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val LOC_REQUEST_CODE = 1000

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    private val distanceMatrixRequest = DistanceMatrixRequest()
    private lateinit var tripId: String
    private var busMarker: Marker? = null
    private lateinit var tripRef: DocumentReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mediaPlayer: MediaPlayer
    private var routeStopsName: MutableList<String> = mutableListOf()
    private var oldRequestCount: Int = 0
    private var nonEmptyRouteStopsName: MutableList<String> = mutableListOf()
    private var routeStopsGeoPoint: MutableList<GeoPoint> = mutableListOf(
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0)
    )
    private var nonEmptyRouteStopsGeoPoint: MutableList<GeoPoint> = routeStopsGeoPoint
    private var journeyPaused: Boolean = false
    private val passengersAtStop: HashMap<String, Int> = HashMap()

    inner class LocationBroadcastReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("ACT_LOC")) {
                val lat: Double? = intent?.getDoubleExtra("latitude", 0.0)
                val lng: Double? = intent?.getDoubleExtra("longitude", 0.0)

                if (lat != null && lng != null) {
                    tripRef.get()
                        .addOnSuccessListener { trip ->

                            // Get bus location and centre map there
                            val location = trip["location"] as GeoPoint
                            if (busMarker == null) {
                                busMarker = mMap.addMarker(
                                    MarkerOptions().position(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        )
                                    ).title("Bus").icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    )
                                )
                                mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 10f
                                    )
                                )
                            } else {
                                busMarker!!.position = LatLng(location.latitude, location.longitude)
                            }

                            CoroutineScope(IO).launch {
                                if ((nonEmptyRouteStopsGeoPoint.size == 0 && routeStopsGeoPoint[0].longitude != 0.0) ||
                                    nonEmptyRouteStopsGeoPoint[0].longitude != 0.0
                                ) {
                                    getNextLocationETA(location)
                                    if (nonEmptyRouteStopsGeoPoint.size > 0) {
                                        getAllLocationsETA(location)
                                    }
                                }
                            }
                        }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun getAllLocationsETA(currLocation: GeoPoint) {
            val indices = mutableListOf<Int>()

            nonEmptyRouteStopsGeoPoint =
                routeStopsGeoPoint.filter { geoPoint ->
                    val idx = routeStopsGeoPoint.indexOf(geoPoint)
                    val stop = routeStopsName[idx]
                    val res = stop in nonEmptyRouteStopsName
                    if (res) {
                        indices.add(idx)
                    }
                    res
                }.toMutableList()

            val locationETAs =
                distanceMatrixRequest.convertFromTimeLengthsToETAs(
                    mutableListOf(currLocation) + nonEmptyRouteStopsGeoPoint
                )

            tripRef.update("etas", locationETAs)

            withContext(Main) {
                updateETAs(locationETAs, indices)
            }
        }

        private fun updateETAs(etas: MutableList<String>, indices: MutableList<Int>) {
            for (i in 0 until etas.size) {
                val item =
                    (rvLocations.findViewHolderForAdapterPosition(indices[i])
                            as LocationInfoAdapter.LocationInfoViewHolder).itemView
                item.tvETA.text = etas[i]
            }
        }

        private suspend fun getNextLocationETA(currLocation: GeoPoint) {
            Log.d("EZRA", "current: $currLocation")

            val (distance, _) = distanceMatrixRequest.getDistanceAndDuration(
                currLocation,
                nonEmptyRouteStopsGeoPoint[0]
            )

            withContext(Main) {
                updateDriverETA(distance)
            }
        }

        private fun updateDriverETA(distance: String) {
            val numDistance = (distance.split(" ")[0]).toDouble()

            if (nonEmptyRouteStopsGeoPoint[0].latitude != 0.0) {
                Log.d(
                    "EZRA",
                    "nextStop: ${nonEmptyRouteStopsName[0]}: ${nonEmptyRouteStopsGeoPoint[0]} $numDistance"
                )
            }

            if (numDistance <= 1.2) {
                Log.d(
                    "EZRA",
                    "updateDriverETA: Arrived at destination ${nonEmptyRouteStopsName[0]}"
                )
                nonEmptyRouteStopsGeoPoint = nonEmptyRouteStopsGeoPoint.drop(1).toMutableList()
                nonEmptyRouteStopsName = nonEmptyRouteStopsName.drop(1).toMutableList()
                if (nonEmptyRouteStopsName.size == 0) {
                    unregisterReceiver(this)
                    busLocationAnnouncement.text = "Arrived at"
                } else {
                    Log.d("EZRA", "updateDriverETA: $nonEmptyRouteStopsName")
                    busLocationAnnouncement.text =
                        if (nonEmptyRouteStopsName.size > 1) resources.getString(R.string.the_next_stop_is) else resources.getString(
                            R.string.the_final_destination_is
                        )

                    val nextStop = nonEmptyRouteStopsName[0]
                    val nextNumPassengers = passengersAtStop.getOrDefault(nextStop, 0)
                    ("$nextStop: " +
                            resources.getQuantityString(
                                R.plurals.passenger_count,
                                nextNumPassengers,
                                nextNumPassengers
                            )).also { busNextStopString.text = it }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        setupPermissions()

        binding = ActivityDriverMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get trip ID from confirmation page
        tripId = intent.getStringExtra("TRIP_ID") ?: ""
        tripRef = db.document(tripId)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        pauseJourneyButton.setOnClickListener { _ -> toggleRequestButton() }

        setStopRequestsCount()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        } else {
            startLocationService()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOC_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOC_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.need_location),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun startLocationService() {
        val receiver = LocationBroadcastReceiver()
        val filter = IntentFilter("ACT_LOC")
        registerReceiver(receiver, filter)
        val intent = Intent(this, TrackingService::class.java)
        startService(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        addRouteStops()
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f))
    }

    private fun addRouteStops() {
        tripRef.get()
            .continueWithTask { task ->
                val document = task.result
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnSuccessListener { route ->
                Log.d(ContentValues.TAG, "Finding route for trip ID $tripId")
                if (route == null) {
                    remaining_stops.text = resources.getString(R.string.route_not_found)
                } else {
                    val stopsData = route["stops"] as List<*>
                    var start = route["start"] as String
                    val destination = route["destination"] as String
                    val stops: List<String> =
                        listOf(start) + stopsData.filterIsInstance<String>() + destination

                    stops.forEach { stop ->
                        addStopOnMap(stop)
                        routeStopsName.add(stop)
                        buildRoute(start, stop, mMap)
                        start = stop
                    }
                }
                setupInfoSheet()
            }
    }

    private fun addStopOnMap(stop: String) {
        db.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding stop $stop agent")
                if (documents.isEmpty) {
                    remaining_stops.text = resources.getString(R.string.stop_not_found)
                }
                documents.forEach { document ->
                    val coordinate: GeoPoint? = document.getGeoPoint("coordinate")

                    if (coordinate != null) {
                        val lat = coordinate.latitude
                        val lng = coordinate.longitude
                        val agent = LatLng(lat, lng)
                        val index = routeStopsName.indexOf(stop)

                        routeStopsGeoPoint[index] = GeoPoint(lat, lng)
                        mMap.addMarker(MarkerOptions().position(agent).title(stop))
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupInfoSheet() {
        // only do this if infoSheet is a bottomSheet
        val params: CoordinatorLayout.LayoutParams =
            infoSheet.layoutParams as CoordinatorLayout.LayoutParams

        if (params.behavior is com.google.android.material.bottomsheet.BottomSheetBehavior) {
            from(infoSheet).peekHeight = 300
            from(infoSheet).state = STATE_COLLAPSED
        }

        db.collection(Collections.ORDERS.toString())
            .whereEqualTo("tripID", tripRef)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding orders for trip ID $tripId")

                // Get the mapping from stop to the number of passengers waiting there
                for (document in documents) {
                    val stop = document.get("pickupLocation") as String
                    val curPassengers = passengersAtStop.getOrDefault(stop, 0)
                    val morePassengers = (document.getLong("numPassengers") ?: 0).toInt()

                    Log.d(
                        ContentValues.TAG, "Processing order for pickup location $stop " +
                                "with $morePassengers passengers"
                    )
                    passengersAtStop[stop] = curPassengers + morePassengers
                }

                // Construct Location Info from the mapping
                val locationInfos = routeStopsName.map { stop ->
                    LocationInfo(
                        stop, passengersAtStop.getOrDefault(stop, -1),
                        "Calculating"
                    )
                }

                nonEmptyRouteStopsName = routeStopsName
                    .filter { name -> passengersAtStop[name] != 0 }
                    .toMutableList()

                rvLocations.adapter = LocationInfoAdapter(locationInfos)
                rvLocations.layoutManager = LinearLayoutManager(this)

                val nextStop = routeStopsName[0]
                val nextNumPassengers = passengersAtStop.getOrDefault(nextStop, 0)
                busNextStopString.text = nextStop + ": " +
                        resources.getQuantityString(
                            R.plurals.passenger_count,
                            nextNumPassengers,
                            nextNumPassengers
                        )

                progress_circular_d.visibility = GONE
                linearLayout_d.visibility = VISIBLE
                infoSheet.visibility = VISIBLE
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleRequestButton() {
        if (journeyPaused) {
            resources.getColorStateList(R.color.softblue)
                .also { pauseJourneyButton.backgroundTintList = it }
            pauseJourneyButton.text = resources.getString(R.string.pause_journey)
            pauseJourneyButton.tooltipText = "Press if bus is stopping in less than 10 mins"
            tripRef.update("impromptuStop", false)
        } else {
            resources.getColorStateList(R.color.light_orange)
                .also { pauseJourneyButton.backgroundTintList = it }
            pauseJourneyButton.text = resources.getString(R.string.resume)
            pauseJourneyButton.tooltipText = "Press if bus is continuing the journey"
            tripRef.update("impromptuStop", true)
            tripRef.update("breakRequests", 0)
        }
        journeyPaused = !journeyPaused
    }

    private fun setStopRequestsCount() {
        tripRef.get().addOnSuccessListener { trip ->
            Log.d(ContentValues.TAG, "Finding trip for trip ID $tripId")
        }

        // add listener to changes on trip to update stop request count
        tripRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val trip = snapshot.data
                val requests = (trip?.get("breakRequests") as Long).toInt()
                when (requests) {
                    0 -> requestsCount.text = resources.getString(R.string.nobody_toilet_request)
                    1 -> requestsCount.text = "1 person is requesting toilet break"
                    else -> requestsCount.text = "$requests people is requesting a toilet break"
                }

                if (requests > 0 && requests > oldRequestCount) {
                    mediaPlayer.start()
                }

                oldRequestCount = requests
                Log.d("Break Request", "break requests update: $requests")
            } else {
                Log.d("Break Request", "Failed getting request number of trip")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }
}
