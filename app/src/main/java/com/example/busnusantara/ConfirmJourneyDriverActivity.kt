package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_confirm_journey_driver.*
import java.text.SimpleDateFormat

class ConfirmJourneyDriverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tripId = getIntent().getStringExtra("ID") ?: ""

        Firebase.firestore.document(tripId).get()
            .continueWithTask { task ->
                val document = task.getResult()
                val date = (document?.get("date") as Timestamp).toDate()
                tripDateField.text = SimpleDateFormat("E dd MMM yyyy").format(date)
                tripDepartureTimeField.text = SimpleDateFormat("hh:mm z").format(date)
                busNumField.text = document.get("busNum").toString()
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener { task ->
                val document = task.getResult()
                startPointField.text = document.get("start").toString()
                destinationPointField.text = document.get("destination").toString()
            }

        setContentView(R.layout.activity_confirm_journey_driver)

        btnToMap.setOnClickListener {
            val intent = Intent(this, DriverMapsActivity::class.java)

            intent.putExtra("TRIP_ID", tripId)
            startActivity(intent)
        }
    }
}
