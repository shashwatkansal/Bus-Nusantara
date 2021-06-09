package com.example.busnusantara

import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.busnusantara.database.Collections
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.busnusantara.databinding.ActivityMapsBinding
import com.example.busnusantara.services.TrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_maps.*

const val LOC_REQUEST_CODE = 1000

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var binding: ActivityMapsBinding
    private var pickUp: String = ""
    private var destination: String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("ACT_LOC")) {
                val lat: Double? = intent?.getDoubleExtra("latitude", 0.0)
                val lng: Double? = intent?.getDoubleExtra("longitude", 0.0)
                if (lat != null && lng != null) {
                    val latLng = LatLng(lat, lng)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissions()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pickUp = getIntent().getStringExtra("PICKUP") ?: ""
        destination = getIntent().getStringExtra("DESTINATION") ?: ""

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
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
                    Toast.makeText(this, "You need the location permission", Toast.LENGTH_SHORT)
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

        addJourneyStops()

        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)

    }

    private fun addJourneyStops() {
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
                        stops = listOf(pickUp) + stops + destination
                        for(stop in stops) {
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
                    if(coordinate != null){
                        val lat = coordinate.getLatitude()
                        val lng = coordinate.getLongitude()
                        val agent = LatLng(lat, lng)

                        mMap.addMarker(MarkerOptions().position(agent).title(stop))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(agent))
                    }
                }
            }
    }
}