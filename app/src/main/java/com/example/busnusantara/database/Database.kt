package com.example.busnusantara.database

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Create a new Order
fun saveOrderToDB(
    routeID: String,
    agentID: String,
    price: Double,
    pickupLocation: String
): String? {

    val order: HashMap<String, Any> = hashMapOf(
        "routeID" to routeID,
        "agentId" to agentID,
        "price" to price,
        "pickupLocation" to pickupLocation
    )
    return saveToDB(order, Collections.ORDERS)
}

// Create a new Route
fun saveRouteToDB(
    destination: String, stops: List<String>
): String? {

    val route: HashMap<String, Any> = hashMapOf(
        "destination" to destination,
        "stops" to stops,
    )

    return saveToDB(route, Collections.ROUTES)
}

private fun saveToDB(data: HashMap<String, Any>, collection: Collections): String? {
    // Add a new document with a generated ID
    var id: String? = null

    Firebase.firestore.collection(collection.toString())
        .add(data)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            id = documentReference.id
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }

    return id
}

// Create a new Agent
fun saveAgentToDB(
    name: String, locationBased: String
): String? {

    val agent: HashMap<String, Any> = hashMapOf(
        "name" to name,
        "locationBased" to locationBased,
    )
    return saveToDB(agent, Collections.AGENTS)
}