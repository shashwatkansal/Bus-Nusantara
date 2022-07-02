package com.example.busnusantara.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.busnusantara.R
import com.example.busnusantara.activities.qr.ScanQRActivity
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
