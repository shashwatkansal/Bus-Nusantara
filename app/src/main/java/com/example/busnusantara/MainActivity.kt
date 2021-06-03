package com.example.busnusantara

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveOrderToDB("1000", "1000", 19.95, "Jakarta")
        saveRouteToDB("Yogyakarta", listOf<String>("Surabaya, Bali, Jakarta"))
        saveAgentToDB("Jeff", "Jakarta")
    }

    // Create a new Order
    private fun saveOrderToDB(
        routeID: String,
        agentID: String,
        price: Double,
        pickupLocation: String
    ) {

        val order = hashMapOf(
            "routeID" to routeID,
            "agentId" to agentID,
            "price" to price,
            "pickupLocation" to pickupLocation
        )

        // Add a new document with a generated ID
        Firebase.firestore.collection("Orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    // Create a new Route
    private fun saveRouteToDB(
        destination: String, stops: List<String>
    ) {

        val route = hashMapOf(
            "destination" to destination,
            "stops" to stops,
        )

        // Add a new document with a generated ID
        Firebase.firestore.collection("Routes")
            .add(route)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    // Create a new Agent
    private fun saveAgentToDB(
        name: String, locationBased: String
    ) {

        val order = hashMapOf(
            "destination" to name,
            "stops" to locationBased,
        )

        // Add a new document with a generated ID
        Firebase.firestore.collection("Agents")
            .add(order)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }


}