package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_confirm_journey_driver.*

class ConfirmJourneyDriverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_driver)

        val tripId = getIntent().getStringExtra("ID") ?: ""
        Firebase.firestore.document(tripId).get()
            .continueWithTask {task ->
                val document = task.getResult()
                tripDateField.text = (document?.get("date") as Timestamp).toDate().toString()
                busNumField.text = document.get("busNum").toString()
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener {task ->
                val document = task.getResult()
                startPointField.text = document.get("start").toString()
                destinationPointField.text = document.get("destination").toString()
            }

        btnToMap.setOnClickListener {
            val intent = Intent(this, DriverMapsActivity::class.java)

            intent.putExtra("START", startPointField.text)
            intent.putExtra("DESTINATION", destinationPointField.text)
            startActivity(intent)
        }
    }
}
