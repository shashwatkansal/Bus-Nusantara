package com.example.busnusantara

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnBusDriver.setOnClickListener {
            val intent = Intent(this, SearchRoute::class.java)
            startActivity(intent)
        }

        btnPassenger.setOnClickListener {
            val intent = Intent(this, SearchRoute::class.java)
            startActivity(intent)
        }

    }

}