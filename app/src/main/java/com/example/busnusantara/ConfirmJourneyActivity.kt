package com.example.busnusantara

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_confirm_journey_passenger.*

class ConfirmJourneyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_journey_passenger)

        journeyIdText.setText(getIntent().getCharSequenceExtra("JOURNEY_ID"))
    }
}