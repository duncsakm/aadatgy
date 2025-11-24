package com.ivi2000.aadatgy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanActivity : BaseActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtInfo: TextView
    private lateinit var btnCancel: Button

    private var cameraExecutor: ExecutorService? = null
    private var processing = false
    private var resultSent = false

    private var mode: String = ""   // "server v normál

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)

        previewView = findViewById(R.id.previewView)
        txtInfo = findViewById(R.id.txtInfo)
        btnCancel = findViewById(R.id.btnCancel)

        btnCancel.setOnClickListener { finish() }

        mode = intent.getStringExtra("MODE") ?: ""

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val scanner = BarcodeScanning.getClient()

            analysis.setAnalyzer(cameraExecutor!!) { imageProxy ->
                if (processing || resultSent) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                processing = true

                val mediaImage = imageProxy.image ?: run {
                    processing = false
                    imageProxy.close()
                    return@setAnalyzer
                }

                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                scanner.process(image)
                    .addOnSuccessListener { codes ->
                        val raw = codes.firstOrNull()?.rawValue
                        if (!raw.isNullOrEmpty() && !resultSent) {
                            resultSent = true
                            returnResult(raw)
                        }
                    }
                    .addOnCompleteListener {
                        processing = false
                        imageProxy.close()
                    }
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    selector,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    getString(R.string.camera_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun returnResult(code: String) {
        val intent = Intent()

        if (mode == "SERVER_QR") {
            intent.putExtra("SERVER_QR_DATA", code)
        } else {
            intent.putExtra("SCANNED_CODE", code)
        }

        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
