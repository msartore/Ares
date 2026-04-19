package dev.msartore.ares.ui.navigation

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.ui.compose.DialogContainer
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.SnackBarDownload
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.ui.compose.TransferDialog
import dev.msartore.ares.ui.home.HomeDestination
import dev.msartore.ares.ui.main.MainEvent
import dev.msartore.ares.ui.serverFinder.ServerFinderDestination
import dev.msartore.ares.ui.settings.SettingsDestination
import dev.msartore.ares.ui.transfers.TransfersDestination
import dev.msartore.ares.viewmodels.MainViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

sealed class MainRoutes(val route: String, val stringId: Int) {
    object HOME : MainRoutes("home", R.string.home)
    object SERVER_FINDER : MainRoutes("server_finder", R.string.server_finder)
    object TRANSFERS : MainRoutes("transfers", R.string.transfers)
    object SETTINGS : MainRoutes("settings", R.string.settings)
}

@Composable
fun AresNavHost(
    mainViewModel: MainViewModel,
    nsdFlow: Flow<NsdServiceInfo?>,
    httpClient: HttpClient,
    navigateToSettingsScreen: () -> Unit,
    onLaunchFilePicker: () -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    onBackgroundClick: () -> Unit,
    onLaunchOssLicenses: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val routes = listOf(
        MainRoutes.HOME,
        MainRoutes.SERVER_FINDER,
        MainRoutes.TRANSFERS,
        MainRoutes.SETTINGS
    )

    NavigationSuiteScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                if (adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 16.dp else 0.dp
            ),
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            navigationRailContentColor = MaterialTheme.colorScheme.secondaryContainer,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        navigationSuiteItems = {
            routes.forEach { route ->
                item(
                    icon = {
                        if (route == MainRoutes.TRANSFERS) {
                            BadgedBox(badge = {
                                val count = mainViewModel.transferredFiles.count { !it.viewed.value }
                                if (count > 0) Badge { TextAuto(text = count.toString()) }
                            }) {
                                Icon(id = R.drawable.download_24px)
                            }
                        } else {
                            val selected = currentRoute == route.route
                            Icon(
                                id = when (route) {
                                    MainRoutes.HOME -> if (selected) R.drawable.home_filled_24px else R.drawable.home_24px
                                    MainRoutes.SERVER_FINDER -> if (selected) R.drawable.wifi_find_filled_24px else R.drawable.wifi_find_24px
                                    else -> if (selected) R.drawable.settings_filled_24px else R.drawable.settings_24px
                                },
                                contentDescription = stringResource(id = route.stringId)
                            )
                        }
                    },
                    label = { Text(stringResource(id = route.stringId)) },
                    selected = currentRoute == route.route,
                    onClick = {
                        if (currentRoute != route.route) {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 16.dp)
        ) {
            val maxWidth = this.maxWidth

            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = MainRoutes.HOME.route
                ) {
                    composable(MainRoutes.HOME.route) {
                        HomeDestination(
                            settings = mainViewModel.settings,
                            mainViewModel = mainViewModel,
                            onLaunchFilePicker = onLaunchFilePicker,
                            onStartServer = onStartServer,
                            onStopServer = onStopServer,
                            onBackgroundClick = onBackgroundClick,
                            maxWidth = maxWidth,
                        )
                    }
                    composable(MainRoutes.SERVER_FINDER.route) {
                        ServerFinderDestination(
                            mainViewModel = mainViewModel,
                            nsdFlow = nsdFlow,
                            httpClient = httpClient,
                            navigateToSettingsScreen = navigateToSettingsScreen,
                        )
                    }
                    composable(MainRoutes.TRANSFERS.route) {
                        TransfersDestination(mainViewModel = mainViewModel)
                    }
                    composable(MainRoutes.SETTINGS.route) {
                        SettingsDestination(
                            settings = mainViewModel.settings,
                            mainViewModel = mainViewModel,
                            onLaunchOssLicenses = onLaunchOssLicenses,
                        )
                    }
                }

                // Download snackbars overlay
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .wrapContentHeight()
                ) {
                    items(
                        count = mainViewModel.listFileDownload.size.value,
                        key = { mainViewModel.listFileDownload.list.elementAt(it).fileData?.uri ?: it }
                    ) { index ->
                        val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(swipeToDismissBoxState.currentValue) {
                            if (swipeToDismissBoxState.currentValue != SwipeToDismissBoxValue.Settled) {
                                mainViewModel.onEvent(
                                    MainEvent.DismissFileDownload(
                                        mainViewModel.listFileDownload.list.elementAt(index)
                                    )
                                )
                            }
                        }

                        if (swipeToDismissBoxState.currentValue == SwipeToDismissBoxValue.Settled) {
                            SwipeToDismissBox(
                                state = swipeToDismissBoxState,
                                backgroundContent = {}
                            ) {
                                mainViewModel.listFileDownload.list.elementAt(index).run {
                                    SnackBarDownload(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        fileDownload = this,
                                        onOpenFile = {
                                            mainViewModel.onEvent(
                                                MainEvent.OpenFileDownload(this)
                                            )
                                        },
                                        onShareFile = {
                                            mainViewModel.onEvent(
                                                MainEvent.ShareFileDownload(this)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Upload progress dialog
                TransferDialog(fileTransfer = KtorService.KtorServer.fileTransfer)

                // QR code dialog
                DialogContainer(
                    status = mainViewModel.qrCodeDialog,
                    dialogProperties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mainViewModel.networkInfo.bitmap.value != null) {
                            Image(
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp)
                                ),
                                bitmap = mainViewModel.networkInfo.bitmap.value!!,
                                contentDescription = "ip"
                            )
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(80.dp))
                        }
                        Row(horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { mainViewModel.qrCodeDialog.value = false }) {
                                TextAuto(id = R.string.close)
                            }
                        }
                    }
                }
            }
        }
    }
}
