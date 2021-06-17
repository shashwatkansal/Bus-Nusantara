package com.example.busnusantara

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.android.synthetic.main.activity_confirm_journey_driver.*
import kotlinx.android.synthetic.main.activity_qr_code_scanner.*
import java.io.File

private const val CAMERA_REQUEST_CODE = 101

class ScanQRActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private var scanPassenger: Boolean = true
    private lateinit var nextActivity: Class<out AppCompatActivity>

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

        // get persistent app-specific file which stores ID from previous QR code scan
        val context: Context = this
        val idFilename = if (scanPassenger) "passengerID" else "driverID"
        var idFile = File(context.filesDir, idFilename)
        if(!idFile.exists()) {
            btnJourney.setVisibility(View.GONE)
            idFile = File.createTempFile(idFilename, null, context.filesDir)
        } else {
            val id = idFile.readText()
            btnJourney.setOnClickListener {
                val intent = Intent(this@ScanQRActivity, nextActivity)
                intent.putExtra("ID", id)
                startActivity(intent)
            }
        }

        setUpPermissions()
        codeScanner(idFile)

        btnContinue.setOnClickListener {
            val intent = Intent(this@ScanQRActivity, nextActivity)
            intent.putExtra("ID",
                if (scanPassenger) "Orders/oolO6KVivO3Z445xu5cW"
                else "Trips/9c4hJnV6gc9FjlWCF6nH")
            startActivity(intent)
        }
    }

    private fun codeScanner(idFile: File) {
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
                    idFile.writeText(it.text)
                    intent.putExtra("ID", it.text)
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
