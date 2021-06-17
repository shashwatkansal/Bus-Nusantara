package com.example.busnusantara

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
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityDriverMapsBinding
import com.example.busnusantara.googleapi.DistanceMatrixRequest
import com.example.busnusantara.services.TrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_driver_maps.*
import kotlinx.android.synthetic.main.activity_driver_maps.infoSheet
import kotlinx.android.synthetic.main.activity_driver_maps.remaining_stops
import kotlinx.android.synthetic.main.activity_driver_maps.rvLocations
import kotlinx.android.synthetic.main.location_info.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

const val LOC_REQUEST_CODE = 1000

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    private val distanceMatrixRequest = DistanceMatrixRequest()
    private lateinit var tripId: String
    private lateinit var tripRef: DocumentReference
    private lateinit var adapter: LocationInfoAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mediaPlayer: MediaPlayer
    private var routeStops: MutableList<String> = mutableListOf()
    private var stopLocations: MutableList<GeoPoint> = mutableListOf(
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0),
        GeoPoint(0.0, 0.0)
    )
    private var journeyPaused: Boolean = false
    private val passengersAtStop: HashMap<String, Int> = HashMap()

    inner class LocationBroadcastReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("ACT_LOC")) {
                val lat: Double? = intent?.getDoubleExtra("latitude", 0.0)
                val lng: Double? = intent?.getDoubleExtra("longitude", 0.0)
                if (lat != null && lng != null) {
/*
    The following line can be commented to prevent unnecessary updates to the database
                    val latLng = LatLng(lat, lng)
                    Firebase.firestore.document(tripId)
                        .update("location", GeoPoint(latLng.latitude, latLng.longitude))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))
*/
                    tripRef.get()
                        .addOnSuccessListener { trip ->
                            val location = trip["location"] as GeoPoint
                            CoroutineScope(IO).launch {
                                if (stopLocations.get(0).longitude != 0.0) {
                                    getNextLocationETA(location)
                                    getAllLocationsETA(location)
                                }
                            }
                        }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun getAllLocationsETA(currLocation: GeoPoint) {
            val locationETAs =
                distanceMatrixRequest.convertFromTimeLengthsToETAs(mutableListOf(currLocation) + stopLocations)

            tripRef.update("etas", locationETAs)

            withContext(Main) {
                updateETAs(locationETAs)
            }
        }


        private suspend fun updateETAs(etas: MutableList<String>) {
            for (i in 0 until etas.size) {
                (rvLocations.findViewHolderForAdapterPosition(i) as LocationInfoAdapter.LocationInfoViewHolder).itemView.tvETA.text =
                    etas[i]
            }
        }


        private suspend fun getNextLocationETA(currLocation: GeoPoint) {
            val (distance, duration) = distanceMatrixRequest.getDistanceAndDuration(
                currLocation,
                stopLocations.get(0)
            )

            withContext(Main) {
                updateDriverETA(distance, duration)
            }
        }

        private suspend fun updateDriverETA(distance: String, duration: String) {
            val numDistance = (distance.split(" ")[0]).toDouble()
            Log.d("EZRA", "updateDriverETA: ${routeStops[0]}: ${stopLocations[0]} $numDistance")
            if (numDistance <= 1.5) {
                Log.d("EZRA", "updateDriverETA: Arrived at destination ${routeStops.get(0)}")
                stopLocations = stopLocations.drop(1).toMutableList()
                routeStops = routeStops.drop(1).toMutableList()
                Log.d("EZRA", "updateDriverETA: ${routeStops}")
                busLocationAnnouncement.text =
                    if (routeStops.size > 1) resources.getString(R.string.the_next_stop_is) else resources.getString(
                        R.string.the_final_destination_is
                    )

                val nextStop = routeStops.get(0)
                val nextNumPassengers = passengersAtStop.getOrDefault(nextStop, 0)
                busNextStopString.text = nextStop + ": " +
                        resources.getQuantityString(
                            R.plurals.passenger_count,
                            nextNumPassengers,
                            nextNumPassengers
                        )
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
        tripId = getIntent().getStringExtra("TRIP_ID") ?: ""
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
                    val start = route["start"] as String
                    val destination = route["destination"] as String
                    val stops: List<String> =
                        listOf(start) + stopsData.filterIsInstance<String>() + destination
                    for (stop in stops) {
                        addStopOnMap(stop)
                        routeStops.add(stop)
                    }
                }

                setupInfoSheet()
            }
    }

    private fun addStopOnMap(stop: String) {
        db.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get().addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding stop $stop agent")
                if (documents.isEmpty) {
                    remaining_stops.text = resources.getString(R.string.stop_not_found)
                }
                for (document in documents) {
                    val coordinate: GeoPoint? = document.getGeoPoint("coordinate")
                    if (coordinate != null) {
                        val lat = coordinate.latitude
                        val lng = coordinate.longitude
                        val agent = LatLng(lat, lng)
                        val index = routeStops.indexOf(stop)
                        stopLocations[index] = GeoPoint(lat, lng)

                        mMap.addMarker(MarkerOptions().position(agent).title(stop))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(agent))
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
            from(infoSheet).peekHeight = 400
            from(infoSheet).state = STATE_COLLAPSED
        }

        db.collection(Collections.ORDERS.toString())
            .whereEqualTo("tripID", tripRef).get()
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
                    passengersAtStop.put(stop, curPassengers + morePassengers)
                }

                // Construct Location Info from the mapping
                val locationInfos = routeStops.map { stop ->
                    LocationInfo(stop, passengersAtStop.getOrDefault(stop, 0), "Calculating")
                }

                adapter = LocationInfoAdapter(locationInfos)

                rvLocations.adapter = adapter
                rvLocations.layoutManager = LinearLayoutManager(this)

                progress_circular_d.visibility = GONE
                linearLayout_d.visibility = VISIBLE
                infoSheet.visibility = VISIBLE

                val nextStop = routeStops.get(0)
                val nextNumPassengers = passengersAtStop.getOrDefault(nextStop, 0)
                busNextStopString.text = nextStop + ": " +
                        resources.getQuantityString(
                            R.plurals.passenger_count,
                            nextNumPassengers,
                            nextNumPassengers
                        )
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleRequestButton() {
        if (journeyPaused) {
            pauseJourneyButton.backgroundTintList = resources.getColorStateList(R.color.softblue)
            pauseJourneyButton.text = resources.getString(R.string.pause_journey)
            pauseJourneyButton.tooltipText = "Press if bus is stopping in less than 10 mins"
            tripRef.update("impromptuStop", false)
        } else {
            pauseJourneyButton.backgroundTintList =
                resources.getColorStateList(R.color.light_orange)
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

            val requests = if (trip == null) 0 else (trip["breakRequests"] as Long).toInt()
            requestsCount.text =
                resources.getQuantityString(R.plurals.num_stop_requests, requests, requests)
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
                requestsCount.text =
                    resources.getQuantityString(R.plurals.num_stop_requests, requests, requests)
                if (requests > 0) {
                    mediaPlayer.start()
                }
                Log.d("Break Request", "break requests update: $requests")
            } else {
                Log.d("Break Request", "Failed getting request number of trip")
            }
        }
    }
}
