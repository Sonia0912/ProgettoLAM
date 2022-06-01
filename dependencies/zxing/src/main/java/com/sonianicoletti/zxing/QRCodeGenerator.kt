package com.sonianicoletti.zxing

import android.graphics.Bitmap

interface QRCodeGenerator {

    fun generateQRCode(content: String, width: Int, height: Int): Bitmap
}