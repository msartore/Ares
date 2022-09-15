package dev.msartore.ares.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.Settings
import dev.msartore.ares.ui.compose.basic.Icon
import dev.msartore.ares.ui.compose.basic.TextAuto

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainUI(
    isLoading: MutableState<Boolean>,
    settings: Settings,
    openUrl: (String) -> Unit,
    onImportFilesClick: () -> Unit,
    onStartServerClick: () -> Unit,
    onStopServerClick: () -> Unit
) {

    val selectedItem = remember { mutableStateOf(MainPages.HOME) }
    val items = listOf(MainPages.HOME, MainPages.SCAN_WIFI, MainPages.SETTINGS)
    val transition = updateTransition(selectedItem.value, label = "")

    Scaffold(
        modifier = Modifier.fillMaxHeight(),
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                id = when(item) {
                                    MainPages.HOME -> {
                                        if (selectedItem.value == item)
                                            R.drawable.home_filled_24px
                                        else
                                            R.drawable.home_24px
                                    }
                                    MainPages.SCAN_WIFI -> {
                                        if (selectedItem.value == item)
                                            R.drawable.wifi_find_filled_24px
                                        else
                                            R.drawable.wifi_find_24px
                                    }
                                    else -> {
                                        if (selectedItem.value == item)
                                            R.drawable.settings_filled_24px
                                        else
                                            R.drawable.settings_24px
                                    }
                                },
                                contentDescription = stringResource(id = item.stringId)
                            )
                        },
                        label = {
                            TextAuto(
                                id = when(item) {
                                    MainPages.HOME -> R.string.home
                                    MainPages.SCAN_WIFI -> R.string.wifi_scan
                                    else -> R.string.settings
                                },
                                interactable = true
                            )
                        },
                        selected = selectedItem.value == item,
                        onClick = {
                            if (selectedItem.value != item)
                                selectedItem.value = item
                        }
                    )
                }
            }
        }
    ) { paddingV ->

        transition.AnimatedContent(
            modifier = Modifier
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingV.calculateBottomPadding()
                )
        ) {
            Column {
                when(it) {
                    MainPages.HOME -> {
                        HomeUI(
                            isLoading = isLoading,
                            onImportFilesClick = onImportFilesClick,
                            onStartServerClick = onStartServerClick,
                            onStopServerClick = onStopServerClick
                        )
                    }
                    MainPages.SCAN_WIFI -> {
                        ScanWifiUI(
                            settings = settings,
                            openUrl = openUrl,
                        )
                    }
                    MainPages.SETTINGS -> {
                        SettingsUI(
                            openUrl = openUrl,
                            settings = settings
                        )
                    }
                }
            }
        }
    }
}

enum class MainPages(val stringId: Int) {
    HOME(R.string.home),
    SCAN_WIFI(R.string.find_devices),
    SETTINGS(R.string.settings)
}