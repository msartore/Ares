package dev.msartore.ares.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.msartore.ares.MainActivity
import dev.msartore.ares.R
import dev.msartore.ares.models.Settings
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.SnackBar
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.isWideView
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

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
    val items = remember { listOf(MainPages.HOME, MainPages.SERVER_FINDER, MainPages.SETTINGS) }
    val transition = updateTransition(selectedItem.value, label = selectedItem.value.name)
    val icon: @Composable (MainPages) -> Unit = {
        Icon(
            id = when(it) {
                MainPages.HOME -> {
                    if (selectedItem.value == it)
                        R.drawable.home_filled_24px
                    else
                        R.drawable.home_24px
                }
                MainPages.SERVER_FINDER -> {
                    if (selectedItem.value == it)
                        R.drawable.wifi_find_filled_24px
                    else
                        R.drawable.wifi_find_24px
                }
                else -> {
                    if (selectedItem.value == it)
                        R.drawable.settings_filled_24px
                    else
                        R.drawable.settings_24px
                }
            },
            contentDescription = stringResource(id = it.stringId)
        )
    }
    val label: @Composable (MainPages) -> Unit = {
        TextAuto(
            id = it.stringId,
            interactable = true,
            style = MaterialTheme.typography.labelLarge
        )
    }
    val onClick: (MainPages) -> Unit = { page ->

        if (selectedItem.value != page)
            selectedItem.value = page
    }
    var maxWidth: Dp? = null
    val mainUI: @Composable (PaddingValues?) -> Unit = {
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = it?.calculateBottomPadding() ?: 16.dp
                )
        ) {
            transition.AnimatedContent {
                Column {
                    when(it) {
                        MainPages.HOME -> {
                            HomeUI(
                                maxWidth = maxWidth,
                                isLoading = isLoading,
                                onImportFilesClick = onImportFilesClick,
                                onStartServerClick = onStartServerClick,
                                onStopServerClick = onStopServerClick
                            )
                        }
                        MainPages.SERVER_FINDER -> {
                            ServerFinderUI(
                                settings = settings,
                                openUrl = openUrl,
                            )
                        }
                        MainPages.SETTINGS -> {
                            SettingsUI(
                                maxWidth = maxWidth,
                                openUrl = openUrl,
                                settings = settings
                            )
                        }
                    }
                }
            }

            SnackBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                visible = MainActivity.MActivity.ipSearchData.isSearching.value != 0
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        TextAuto(id = R.string.scanning_servers)

                        TextAuto(text = "${stringResource(id = R.string.ip_left_check)}: ${MainActivity.MActivity.ipSearchData.ipLeft.value}")
                    }

                    Icon(
                        id = R.drawable.cancel_24px
                    ) {
                        scope.launch {
                            MainActivity.MActivity.ipSearchData.job.cancelAndJoin()
                            MainActivity.MActivity.ipSearchData.isSearching.value = 0
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints {

        maxWidth = this.maxWidth

        if (maxWidth?.isWideView() == true)
            Row {
                NavigationRail(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth()
                ) {
                    items.forEach { item ->
                        NavigationRailItem(
                            icon = { icon(item) },
                            label = { label(item) },
                            onClick = { onClick(item) },
                            selected = selectedItem.value == item
                        )
                    }
                }

                mainUI(null)
            }
        else
            Scaffold(
                modifier = Modifier.fillMaxHeight(),
                bottomBar = {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { icon(item) },
                                label = { label(item) },
                                onClick = { onClick(item) },
                                selected = selectedItem.value == item
                            )
                        }
                    }
                }
            ) {
                mainUI(it)
            }
    }
}

enum class MainPages(val stringId: Int) {
    HOME(R.string.home),
    SERVER_FINDER(R.string.server_finder),
    SETTINGS(R.string.settings)
}