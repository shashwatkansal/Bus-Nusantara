package com.example.busnusantara

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.android.synthetic.main.activity_qr_code_scanner.*

private const val CAMERA_REQUEST_CODE = 101

class ScanQRActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private var scanPassenger: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)

        scanPassenger = getIntent().getBooleanExtra("SCAN_PASSENGER", true)
        tvQRScan.text = if (scanPassenger) {
            resources.getString(R.string.scan_qr_passenger)
        } else {
            resources.getString(R.string.scan_qr_driver)
        }
        setUpPermissions()
        codeScanner()
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
                        this@ScanQRActivity,
                        if (scanPassenger) ConfirmJourneyPassengerActivity::class.java
                                      else ConfirmJourneyDriverActivity::class.java)
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
