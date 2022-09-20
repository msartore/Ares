package dev.msartore.ares.server

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.msartore.ares.models.FileDataJson

data class ServerInfo(
    val fileList: SnapshotStateList<FileDataJson> = SnapshotStateList(),
    val ip: String
)
