package com.example.busnusantara

import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.busnusantara.database.Collections
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.busnusantara.databinding.ActivityDriverMapsBinding
import com.example.busnusantara.services.TrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_driver_maps.*
import kotlinx.android.synthetic.main.activity_driver_maps.bottomSheet
import kotlinx.android.synthetic.main.activity_driver_maps.remaining_stops
import kotlinx.android.synthetic.main.activity_driver_maps.rvLocations
import kotlinx.android.synthetic.main.activity_passenger_maps.*

const val LOC_REQUEST_CODE = 1000

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    private lateinit var tripId: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationInfoAdapter: LocationInfoAdapter
    private val routeStops: MutableList<String> = mutableListOf()
    private var journeyPaused: Boolean = false

    inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("ACT_LOC")) {
                val lat: Double? = intent?.getDoubleExtra("latitude", 0.0)
                val lng: Double? = intent?.getDoubleExtra("longitude", 0.0)
                if (lat != null && lng != null) {
                    val latLng = LatLng(lat, lng)
                    remaining_stops.text = "Your location is: $latLng"
//                    The following line can be commented to prevent unnecessary updates to the database
//                    Firebase.firestore.document(tripId)
//                        .update("location", GeoPoint(latLng.latitude, latLng.longitude))
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))
                }
            }
        }
    }

    private fun toggleRequestButton() {
        if (journeyPaused) {
            pauseJourneyButton.backgroundTintList = resources.getColorStateList(R.color.softblue)
            pauseJourneyButton.text = resources.getString(R.string.pause_journey)
            Firebase.firestore.document("/Buses/s3COY5sKL9HX6JLdD90p")
                .update("impromptuStop", false)
        } else {
            pauseJourneyButton.backgroundTintList =
                resources.getColorStateList(R.color.light_orange)
            pauseJourneyButton.text = resources.getString(R.string.resume)
            Firebase.firestore.document("/Buses/s3COY5sKL9HX6JLdD90p")
                .update("impromptuStop", true)
        }
        journeyPaused = !journeyPaused
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissions()

        binding = ActivityDriverMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = getIntent().getStringExtra("TRIP_ID") ?: ""

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        pauseJourneyButton.setOnClickListener { _ -> toggleRequestButton() }


//        TODO: get this to work
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                When the data changes this function is called
                val requests = snapshot.value as Int
                Log.d("Get requests number", "onDataChange: $requests")
//                requestsCount.text = "Stop requests: $requests"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Get requests number", "Failed for some reason", error.toException())
            }
        }

        Firebase.database.getReference("Buses/s3COY5sKL9HX6JLdD90p/breakRequests")
            .addValueEventListener(postListener)

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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f))
    }

    private fun addRouteStops() {
        Firebase.firestore.document(tripId).get()
            .continueWithTask { task ->
                val document = task.getResult()
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
                setupBottomSheet()
            }
    }

    private fun addStopOnMap(stop: String) {
        Firebase.firestore.collection(Collections.AGENTS.toString())
            .whereEqualTo("locationBased", stop)
            .get().addOnSuccessListener { documents ->
                Log.d(ContentValues.TAG, "Finding stop $stop agent")
                if (documents.isEmpty) {
                    remaining_stops.text = resources.getString(R.string.stop_not_found)
                }
                for (document in documents) {
                    val coordinate: GeoPoint? = document.getGeoPoint("coordinate")
                    if (coordinate != null) {
                        val lat = coordinate.getLatitude()
                        val lng = coordinate.getLongitude()
                        val agent = LatLng(lat, lng)

                        mMap.addMarker(MarkerOptions().position(agent).title(stop))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(agent))
                    }
                }
            }
    }

    private fun setupBottomSheet() {
        from(bottomSheet).peekHeight = 150
        from(bottomSheet).state = STATE_COLLAPSED

        locationInfoAdapter = LocationInfoAdapter(routeStops.map { stop ->
            LocationInfo(stop, 3)
        })
        rvLocations.adapter = locationInfoAdapter
        rvLocations.layoutManager = LinearLayoutManager(this)
    }
}
