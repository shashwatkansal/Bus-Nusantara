package com.example.busnusantara

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import kotlinx.android.synthetic.main.activity_confirm_journey_driver.*
import kotlinx.android.synthetic.main.activity_qr_code_scanner.*


private const val CAMERA_REQUEST_CODE = 101

class ScanQRActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private var scanPassenger: Boolean = true
    private lateinit var nextActivity: Class<out AppCompatActivity>
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)

        scanPassenger = getIntent().getBooleanExtra("SCAN_PASSENGER", true)

        tvQRScan.text = if (scanPassenger) {
            resources.getString(R.string.scan_qr_passenger)
        } else {
            resources.getString(R.string.scan_qr_driver)
        }

        nextActivity = if (scanPassenger) ConfirmJourneyPassengerActivity::class.java
            else ConfirmJourneyDriverActivity::class.java

        // Set previous journey button based on cached ID
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val cachedId = getCachedId()
        if(cachedId == "") {
            btnJourney.setVisibility(View.GONE)
        } else {
            // TODO("setVisibility to View.VISIBLE")
            btnJourney.setVisibility(View.GONE)
            btnJourney.setOnClickListener {
                val intent = Intent(this@ScanQRActivity, nextActivity)
                intent.putExtra("ID", cachedId)
                startActivity(intent)
            }
        }

        setUpPermissions()
        codeScanner()

        btnContinue.setOnClickListener {
            val id = if (scanPassenger) "Orders/oolO6KVivO3Z445xu5cW" else "Trips/9c4hJnV6gc9FjlWCF6nH"
            putCachedId(id)
            val intent = Intent(this@ScanQRActivity, nextActivity)
            intent.putExtra("ID", id)
            startActivity(intent)
        }
    }

    // get cached ID of previous QR code scan from shared preferences
    private fun getCachedId(): String {
        val key = if (scanPassenger) "cachedPassengerID" else "cachedDriverID"
        val cachedId = sharedPref.getString(key, "")
        return if(cachedId != null) cachedId else ""
    }

    // store ID to shared preferences
    private fun putCachedId(id: String) {
        val key = if (scanPassenger) "cachedPassengerID" else "cachedDriverID"
        with (sharedPref.edit()) {
            putString(key, id)
            apply()
        }
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, scanner_view)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            // On a successful QR scan, pass the scanned ID to the confirmation activity
            decodeCallback = DecodeCallback {
                runOnUiThread {
                    val intent = Intent(
                        this@ScanQRActivity, nextActivity)
                    val id = it.text
                    putCachedId(id)
                    intent.putExtra("ID", id)
                    startActivity(intent)
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setUpPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, resources.getString(R.string.need_camera), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
