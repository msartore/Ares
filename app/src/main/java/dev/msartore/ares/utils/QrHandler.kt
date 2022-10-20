package dev.msartore.ares.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageConfig.BLACK
import com.google.zxing.client.j2se.MatrixToImageConfig.WHITE
import java.util.Hashtable

fun encodeAsBitmap(
    contents: String?,
    desiredWidth: Int,
    desiredHeight: Int,
    format: BarcodeFormat = BarcodeFormat.QR_CODE
): Bitmap? {
    val hints: Hashtable<EncodeHintType, Any> = Hashtable<EncodeHintType, Any>(2).apply {
        this[EncodeHintType.CHARACTER_SET] = "UTF-8"
    }
    val result = MultiFormatWriter().encode(contents, format, desiredWidth, desiredHeight, hints)
    val width = result.width
    val height = result.height
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (result[x, y]) BLACK else WHITE
        }
    }

    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, width, 0, 0, width, height)
    }
}