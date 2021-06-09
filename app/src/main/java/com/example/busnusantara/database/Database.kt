package com.example.busnusantara.database


import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap

/* *
 Create a new Order
 */
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
    return saveToDB(order, Collections.ORDERS).toString()
}

/* *
 Create a new Route
 */
fun saveRouteToDB(
    destination: String, stops: List<String>
): String? {

    val route: HashMap<String, Any> = hashMapOf(
        "destination" to destination,
        "stops" to stops,
    )

    return saveToDB(route, Collections.ROUTES)
}

private fun saveToDB(data: HashMap<String, Any>, collection: Collections): String {
    // Add a new document with a generated ID
    var id: String? = null

    return Firebase.firestore.collection(collection.toString())
        .add(data)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            id = documentReference.id
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
        .result.toString()
}

// Create a new Agent
fun saveAgentToDB(
    name: String, locationBased: String
): String {

    val agent: HashMap<String, Any> = hashMapOf(
        "name" to name,
        "locationBased" to locationBased,
    )
    return saveToDB(agent, Collections.AGENTS)
}

// Save the driver
fun saveTripToDB(
    busNum: String, location: GeoPoint, routeID: String
): String {
    val collection = Collections.ROUTES.toString()
    val routeIDRef = Firebase.firestore.collection(collection)
        .document(routeID)


    val trip: HashMap<String, Any> = hashMapOf(
        "busNum" to busNum,
        "location" to location,
        "routeID" to routeIDRef, // Needs to be a reference
        "date" to Date()
    )

    return saveToDB(trip, Collections.TRIPS)
}

fun updateBusLocation(busTripID: String, newLocation: GeoPoint) {
    val collectionTrips = Collections.TRIPS.toString()

    Firebase.firestore.collection(collectionTrips)
        .document(busTripID)
        .update("location", newLocation)
        .addOnSuccessListener { _ ->
            println("Bus Location of trip $busTripID updated to $newLocation")
        }
}