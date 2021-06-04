package com.example.busnusantara

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import com.example.busnusantara.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnSearchRoute.setOnClickListener {
            val startLocation = etStartLocation.text.toString()
            val destination = etDestination.text.toString()

            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                Firebase.firestore.collection(Collections.ROUTES.toString())
                    .whereEqualTo("destination", destination)
                    .get().addOnSuccessListener { documents ->
                        Log.d(ContentValues.TAG, "Finding all routes with $destination dest")
                        if (documents.isEmpty) {
                            textView.text = "No route was found. Please try again"
                        }
                        for (document in documents) {
                            textView.text =
                                "There is a route going through: " + document.data.get("stops")
                                    .toString()
                            Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                        }
                    }
            }
        }
    }
}