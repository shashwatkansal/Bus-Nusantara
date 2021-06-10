package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_confirm_journey_passenger.*

class ConfirmJourneyPassengerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_passenger)

        val orderId = getIntent().getStringExtra("ID") ?: ""
        Firebase.firestore.collection(Collections.ORDERS.toString())
            .document(orderId).get()
            .continueWithTask {task ->
                val document = task.getResult()
                startPointField.text = document?.get("pickupLocation").toString()
                numPassengersField.text = document?.get("numPassengers").toString()
                val tripId = document?.get("tripID") as DocumentReference
                tripId.get()
            }
            .continueWithTask {task ->
                val document = task.getResult()
                tripDateField.text = (document?.get("date") as Timestamp).toDate().toString()
                busNumField.text = document.get("busNum").toString()
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener {task ->
                val document = task.result
                destinationPointField.text = document?.get("destination").toString()
            }

        btnToMap.setOnClickListener {
            val intent = Intent(this, PassengerMapsActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }
    }
}
