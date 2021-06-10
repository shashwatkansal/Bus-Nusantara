package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.database.saveTripToDB
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        saveTripToDB(
            "Bus21XD",
            GeoPoint(21.521, 12.214), "IT5cRoAVJjl16gy0bfGh"
        )

        btnBusDriver.setOnClickListener {
            val intent = Intent(this, ConfirmJourneyDriverActivity::class.java)
            startActivity(intent)
        }

        btnPassenger.setOnClickListener {
            val intent = Intent(this, PassengerMapsActivity::class.java)
            startActivity(intent)
        }

    }

}
