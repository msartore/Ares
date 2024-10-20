package dev.msartore.ares.models

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.msartore.ares.utils.isValidServerIP

@ExperimentalGetImage
class BarcodeImageAnalyzer(
    private val onQRFound: ((String, String) -> Unit)? = null,
) : ImageAnalysis.Analyzer {
    private var isIPFound = false
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { barcodes ->

                    for (barcode in barcodes) {

                        when (barcode.valueType) {
                            Barcode.TYPE_URL -> {
                                barcode.url?.url.toString().let {

                                    it.substring(7).split(":").let { server ->
                                        if (isValidServerIP(server[0]) && !isIPFound) {
                                            isIPFound = true
                                            onQRFound?.invoke(server[0], server[1])
                                            return@addOnSuccessListener
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    it.printStackTrace()
                    mediaImage.close()
                    imageProxy.close()
                }.addOnCompleteListener {
                    mediaImage.close()
                    imageProxy.close()
                }
        }
    }
}