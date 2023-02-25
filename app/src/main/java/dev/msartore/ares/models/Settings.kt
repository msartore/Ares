package dev.msartore.ares.models

import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.utils.readBool
import dev.msartore.ares.utils.readInt
import dev.msartore.ares.utils.write

class Settings(
    private val dataStore: DataStore<Preferences>
) {
    private val timeout = 150
    private val port = 7070

    var findServersAtStart: MutableState<Boolean> = mutableStateOf(false)
    var ipTimeout: MutableState<Int> = mutableStateOf(timeout)
    var isMaterialYouEnabled: MutableState<Boolean> = mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    var serverPortNumber: MutableState<Int> = mutableStateOf(port)
    var removeWifiRestriction: MutableState<Boolean> = mutableStateOf(false)
    var serverAutoStartup: MutableState<Boolean> = mutableStateOf(false)

    @androidx.camera.core.ExperimentalGetImage
    suspend fun update() {
        findServersAtStart.value = dataStore.readBool(Keys.FindServersAtStart.key) == true
        ipTimeout.value = dataStore.readInt(Keys.IPTimeout.key) ?: timeout
        isMaterialYouEnabled.value = dataStore.readBool(Keys.MaterialYou.key) == true
        removeWifiRestriction.value = dataStore.readBool(Keys.RemoveWifiRestriction.key) == true
        serverAutoStartup.value = dataStore.readBool(Keys.ServerAutoStartup.key) == true
        serverPortNumber.value = dataStore.readInt(Keys.ServerPortNumber.key) ?: port
        KtorService.KtorServer.port = serverPortNumber.value
    }

    suspend fun <T> save(key: Keys, value: MutableState<T>) {
        dataStore.write(key.key, value.value)
    }

    enum class Keys(val key: String) {
        FindServersAtStart("find_servers_at_start"),
        ServerPortNumber("server_port_number"),
        IPTimeout("ip_timeout"),
        RemoveWifiRestriction("remove_wifi_restriction"),
        ServerAutoStartup("server_auto_startup"),
        MaterialYou("material_you")
    }
}
