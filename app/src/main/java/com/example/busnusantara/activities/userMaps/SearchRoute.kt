package com.example.busnusantara.activities.userMaps

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.R
import com.example.busnusantara.activities.qr.ScanQRActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class SearchRoute : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSearchRoute.setOnClickListener {
            val startLocation = etStartLocation.text.toString()
            val destination = etDestination.text.toString()

            // Remove any whitespace from inputs
            startLocation.replace("\\s".toRegex(), "")
            destination.replace("\\s".toRegex(), "")

            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                Firebase.firestore.collection(Collections.ROUTES.toString())
                    .whereEqualTo("destination", destination)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d(ContentValues.TAG, "Finding all routes with $destination dest")

                        if (documents.isEmpty) {
                            remaining_stops.text = "No route was found. Please try again"
                        }
                        handleDocuments(documents, startLocation, destination)
                    }
            }
        }

        btnToQR.setOnClickListener {
            val intent = Intent(this, ScanQRActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleDocuments(
        documents: QuerySnapshot,
        startLocation: String,
        destination: String
    ) {
        documents.forEach { document ->
            val stopsData = document.data["stops"]
            if (stopsData is List<*>) {
                var stops: List<String> = stopsData.filterIsInstance<String>()
                stops = listOf(startLocation) + stops + destination
                val stopsAdapter =
                    ArrayAdapter(this, android.R.layout.simple_list_item_1, stops)
                stopsList.adapter = stopsAdapter
                remaining_stops.text = "Route found."
                Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
            }
        }
    }
}