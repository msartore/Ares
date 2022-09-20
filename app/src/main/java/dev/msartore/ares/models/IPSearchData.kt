package dev.msartore.ares.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.msartore.ares.server.ServerInfo
import kotlinx.coroutines.Job

data class IPSearchData(
    val ipList: ConcurrentMutableList<ServerInfo> = ConcurrentMutableList(),
    val isSearching: MutableState<Int> = mutableStateOf(0),
    val ipLeft: MutableState<Int> = mutableStateOf(0),
    var job: Job = Job()
)
