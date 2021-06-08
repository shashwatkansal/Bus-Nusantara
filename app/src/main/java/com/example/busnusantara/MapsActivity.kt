package com.example.busnusantara

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.busnusantara.database.Collections
import com.example.busnusantara.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

private const val LOCATION_REQUEST_CODE = 200

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var pickUp: String = ""
    private var destination: String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        addJourneyStops()

        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)

        TODO("debug fetching current location, UI keeps on crashing")
//        addCurrentLocation();

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


    private fun addCurrentLocation() {
        checkLocationPermission()
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                val lat = location.getLatitude();
                val lng = location.getLongitude();
                val myLocation = LatLng(lat, lng)
                mMap.addMarker(MarkerOptions().position(myLocation).title("current position"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
            }
            .addOnFailureListener {
                Toast.makeText(this, "failed to fetch current location", Toast.LENGTH_SHORT)
                    .show()
            }

        TODO("create location listener")
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                // permission denied
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need the location permission", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}