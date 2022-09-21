package dev.msartore.ares.models

import android.os.Build
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
    var isMaterialYouEnabled: MutableState<Boolean> = mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    suspend fun update() {
        findServersAtStart.value = dataStore.readBool(Keys.DownloadMedia.key) == true
        ipTimeout.value = dataStore.readInt(Keys.IPTimeout.key) ?: timeout
        isMaterialYouEnabled.value = dataStore.readBool(Keys.MaterialYou.key) == true
    }

    suspend fun save() {
        dataStore.write(Keys.DownloadMedia.key, findServersAtStart.value)
        dataStore.write(Keys.IPTimeout.key, ipTimeout.value)
        dataStore.write(Keys.MaterialYou.key, isMaterialYouEnabled.value)
    }

    enum class Keys(val key: String) {
        DownloadMedia("download_media"),
        IPTimeout("ip_timeout"),
        MaterialYou("material_you")
    }
}
