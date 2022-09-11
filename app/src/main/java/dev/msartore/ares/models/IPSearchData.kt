package dev.msartore.ares.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Job

data class IPSearchData(
    val ipList: ConcurrentMutableList<String> = ConcurrentMutableList(),
    val isSearching: MutableState<Int> = mutableStateOf(0),
    val ipLeft: MutableState<Int> = mutableStateOf(0),
    var job: Job = Job()
)
