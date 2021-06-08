package com.example.busnusantara

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_confirm_journey_passenger.*

class ConfirmJourneyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_passenger)

        val bookingId = getIntent().getStringExtra("BOOKING_ID") ?: ""
        val orders = Firebase.firestore.collection(Collections.ORDERS.toString())
        Firebase.firestore.document(bookingId).get()
//            .addOnSuccessListener{document ->
//                startPointField.text = document.get("pickupLocation").toString()
//                //destinationPointField.text = document.get("destination").toString()
//                //tripDateField.text = document.get("date").toString()
//                numPassengersField.text = document.get("numPassengers").toString()
//                //busNumField.text = document.get("busNum").toString()
//            }
//            .addOnFailureListener { exception ->
//                Log.e("CJ", exception.message ?: "")
//            }
            .continueWithTask {task ->
                val document = task.getResult()
                startPointField.text = document?.get("pickupLocation").toString()
                numPassengersField.text = document?.get("numPassengers").toString()
                val tripId = document?.get("tripID") as DocumentReference
                tripId.get()
            }
            .continueWithTask {task ->
                Log.d("DEBUGCONFIRM", "stage 1 success")
                val document = task.getResult()
                tripDateField.text = (document?.get("date") as Timestamp).toDate().toString()
                busNumField.text = document?.get("busNum").toString()
                val routeId = document?.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener {task ->
                val document = task.result
                destinationPointField.text = document?.get("destination").toString()
            }
    }
}