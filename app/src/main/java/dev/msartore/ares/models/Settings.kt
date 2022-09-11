package dev.msartore.ares.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.msartore.ares.utils.readBool
import dev.msartore.ares.utils.readInt
import dev.msartore.ares.utils.write

class Settings(
    private val dataStore: DataStore<Preferences>
) {

    private val timeout = 150

    var findServersAtStart: MutableState<Boolean> = mutableStateOf(false)
    var ipTimeout: MutableState<Int> = mutableStateOf(timeout)

    suspend fun update() {
        findServersAtStart.value = dataStore.readBool(Keys.DownloadMedia.key) == true
        ipTimeout.value = dataStore.readInt(Keys.IPTimeout.key) ?: timeout
    }

    suspend fun save() {
        dataStore.write(Keys.DownloadMedia.key, findServersAtStart.value)
        dataStore.write(Keys.IPTimeout.key, ipTimeout.value)
    }

    enum class Keys(val key: String) {
        DownloadMedia("download_media"),
        IPTimeout("ip_timeout")
    }
}
