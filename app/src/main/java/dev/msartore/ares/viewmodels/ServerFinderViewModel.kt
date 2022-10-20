package dev.msartore.ares.viewmodels

import androidx.lifecycle.ViewModel
import dev.msartore.ares.models.IPSearchData
import dev.msartore.ares.models.QrReadingProcess

class ServerFinderViewModel : ViewModel() {

    val ipSearchData = IPSearchData()
    val qrReadingProcess = QrReadingProcess()

    fun scanQRCode() {
        qrReadingProcess.isReadingQR.value = true
    }
}