package com.example.busnusantara

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_confirm_journey_passenger.*
import java.text.SimpleDateFormat
import java.util.*

class ConfirmJourneyPassengerActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var orderRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_passenger)

        val orderId = intent.getStringExtra("ID") ?: ""
        orderRef = db.document(orderId)

        // Display required information
        displayTripInformation()
        displayBusAmenities()


        btnToMap.setOnClickListener {
            val intent = Intent(this, PassengerMapsActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }
    }

    private fun displayBusAmenities() {
        orderRef.get()
            .continueWithTask { task ->
                val order = task.result
                order.getDocumentReference("tripID")?.get()
            }
            .continueWithTask { task ->
                val trip = task.result
                val busNum: String? = trip.getString("busNum")
                db.collection(Collections.BUSES.toString())
                    .whereEqualTo("busNum", busNum)
                    .get()
            }
            .addOnSuccessListener { buses ->
                if (buses.isEmpty || buses.size() > 1) {
                    Log.e(TAG, "Duplicates of busNum exist in database!")
                }
                for (bus in buses) {
                    busTypePointField.text =
                        if (bus["executive"] as Boolean)
                            "Executive Bus" else "Standard Bus"
                    val wifiSymbol = if (bus["wifi"] as Boolean)
                        R.drawable.wifi else R.drawable.no_wifi
                    wifiIcon.setImageResource(wifiSymbol)
                    val toiletSymbol = if (bus["toilets"] as Boolean)
                        R.drawable.ic_toilet else R.drawable.ic_notoilet
                    toiletIcon.setImageResource(toiletSymbol)
                }
            }
    }

    private fun displayTripInformation() {
        orderRef.get()
            .continueWithTask { task ->
                val document = task.result
                startPointField.text = document?.get("pickupLocation").toString()
                numPassengersField.text = document?.get("numPassengers").toString()
                val tripId = document?.get("tripID") as DocumentReference
                tripId.get()
            }
            .continueWithTask { task ->
                val document = task.result
                val date = (document?.get("date") as Timestamp).toDate()
                tripDateField.text =
                    SimpleDateFormat("E dd MMM yyyy", Locale.getDefault()).format(date)
                tripTimeField.text =
                    SimpleDateFormat("hh:mm", Locale.getDefault()).format(date)
                busNumField.text = document.get("busNum").toString()
                val routeId = document.get("routeID") as DocumentReference
                routeId.get()
            }
            .addOnCompleteListener { task ->
                val document = task.result
                destinationPointField.text = document?.get("destination").toString()
            }
    }
}
