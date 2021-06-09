package com.example.busnusantara

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.security.AccessController.getContext


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
                    .get().addOnSuccessListener { documents ->
                        Log.d(ContentValues.TAG, "Finding all routes with $destination dest")
                        if (documents.isEmpty) {
                            textView.text = "No route was found. Please try again"
                        }
                        for (document in documents) {
                            val stopsData = document.data.get("stops")
                            if (stopsData is List<*>) {
                                var stops: List<String> = stopsData.filterIsInstance<String>()
                                stops = listOf(startLocation) + stops + destination
                                val stopsAdapter =
                                    ArrayAdapter(this, android.R.layout.simple_list_item_1, stops)
                                stopsList.setAdapter(stopsAdapter)
                                textView.text = "Route found."
                                Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                            }
                        }
                    }
            }
        }

        btnToQR.setOnClickListener {
            val intent = Intent(this, QRCodeScanner::class.java)
            startActivity(intent)
        }

        btnToMap.setOnClickListener {
            val intent = Intent(this, PassengerMapsActivity::class.java)
            startActivity(intent)
        }
    }
}