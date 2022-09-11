package dev.msartore.ares.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun cor(block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.Main).launch {
        block()
    }

fun work(block: suspend CoroutineScope.() -> Unit) =
    cor {
        withContext(Dispatchers.IO) {
            block()
        }
    }