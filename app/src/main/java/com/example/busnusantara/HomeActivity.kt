package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intent = Intent(this, ScanQRActivity::class.java)

        btnBusDriver.setOnClickListener {
            intent.putExtra("SCAN_PASSENGER", false)
            startActivity(intent)
        }

        btnPassenger.setOnClickListener {
            intent.putExtra("SCAN_PASSENGER", true)
            startActivity(intent)
        }
    }
}
