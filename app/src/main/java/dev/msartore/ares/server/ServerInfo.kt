package dev.msartore.ares.server

import android.os.CountDownTimer
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.msartore.ares.models.FileDataJson

data class ServerInfo(
    val ip: String,
    val port: String,
    val fileList: SnapshotStateList<FileDataJson> = SnapshotStateList(),
)

data class ServerTimer(
    var timerScheduler: CountDownTimer? = null,
    var millsToWait: Long = 0,
)