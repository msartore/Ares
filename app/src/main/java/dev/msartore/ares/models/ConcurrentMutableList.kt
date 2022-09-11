package dev.msartore.ares.models

import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.ConcurrentLinkedQueue

class ConcurrentMutableList<T> {

    val list = ConcurrentLinkedQueue<T>()
    val size = mutableStateOf(list.size)

    private fun updateSize() {
        size.value = list.size
    }

    fun add(item: T) {
        list.add(item)
        updateSize()
    }

    fun addAll(items: Collection<T>) {
        list.addAll(items)
        updateSize()
    }

    fun removeIf(predicate: (T) -> Boolean) {
        list.removeIf { predicate(it) }
        updateSize()
    }

    fun clear() {
        list.clear()
        updateSize()
    }
}