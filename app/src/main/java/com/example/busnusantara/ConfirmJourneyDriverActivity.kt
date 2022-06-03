package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_confirm_journey_driver.*
import java.text.SimpleDateFormat
import java.util.*

class ConfirmJourneyDriverActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_driver)

        val tripId = intent.getStringExtra("ID") ?: ""

        db.document(tripId).get()
            .continueWithTask { task ->
                val document = task.result
                val date = (document?.get("date") as Timestamp).toDate()
                tripDateField.text =
                    SimpleDateFormat("E dd MMM yyyy", Locale.getDefault()).format(date)
                tripDepartureTimeField.text =
                    SimpleDateFormat("hh:mm", Locale.getDefault()).format(date)
                busNumField.text = document.get("busNum").toString()
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener { task ->
                val document = task.result
                startPointField.text = document.get("start").toString()
                destinationPointField.text = document.get("destination").toString()
                progress_circular.visibility = GONE
                confirmation_info.visibility = VISIBLE
            }


        btnToMap.setOnClickListener {
            val intent = Intent(this, DriverMapsActivity::class.java)

            intent.putExtra("TRIP_ID", tripId)
            startActivity(intent)
        }

    }
}
