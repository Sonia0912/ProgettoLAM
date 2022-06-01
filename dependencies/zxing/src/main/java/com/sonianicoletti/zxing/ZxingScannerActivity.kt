package com.sonianicoletti.zxing

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ZxingScannerActivity : AppCompatActivity() {

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            val intent = Intent()
            intent.putExtra("qrcontent", result.contents)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scan()
    }

    private fun scan() {
        val options = ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        barcodeLauncher.launch(options)
    }
}
