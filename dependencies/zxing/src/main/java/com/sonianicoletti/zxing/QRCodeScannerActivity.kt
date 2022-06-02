package com.sonianicoletti.zxing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class QRCodeScannerActivity : AppCompatActivity() {

    private val qrCodeScannerLauncher = registerForActivityResult(ScanContract()) { onQrCodeScanned(it)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchScanner()
    }

    private fun launchScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setBeepEnabled(false)
            .setOrientationLocked(false)
        qrCodeScannerLauncher.launch(options)
    }

    private fun onQrCodeScanned(result: ScanIntentResult) {
        if (result.contents != null) {
            finishWithResult(result.contents)
        } else {
            finishCancelled()
        }
    }

    private fun finishWithResult(resultContents: String) {
        val intent = Intent()
        intent.putExtra(EXTRA_QR_CONTENT, resultContents)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun finishCancelled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        const val EXTRA_QR_CONTENT = "EXTRA_QR_CONTENT"
    }
}
