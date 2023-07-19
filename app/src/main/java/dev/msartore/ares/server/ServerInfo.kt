package dev.msartore.ares.server

import android.os.CountDownTimer
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.msartore.ares.models.FileDataJson

data class ServerInfo(
    val fileList: SnapshotStateList<FileDataJson> = SnapshotStateList(), val ip: String
)

data class ServerTimer(
    var timerScheduler: CountDownTimer? = null,
    var millsToWait: Long = 0,
)