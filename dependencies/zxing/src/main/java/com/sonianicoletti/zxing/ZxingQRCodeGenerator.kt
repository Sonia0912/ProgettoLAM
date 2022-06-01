package com.sonianicoletti.zxing

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import javax.inject.Inject

class ZxingQRCodeGenerator @Inject constructor() : QRCodeGenerator {

    private val barcodeEncoder = BarcodeEncoder()

    override fun generateQRCode(content: String, width: Int, height: Int): Bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, width, height)
}
