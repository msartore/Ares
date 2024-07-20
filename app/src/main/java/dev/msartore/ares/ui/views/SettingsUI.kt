package dev.msartore.ares.ui.views

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.msartore.ares.R
import dev.msartore.ares.models.Settings
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.SettingsItem
import dev.msartore.ares.ui.compose.SettingsItemInput
import dev.msartore.ares.ui.compose.SettingsItemSwitch
import dev.msartore.ares.ui.compose.SettingsItemTimer
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.packageInfo
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.SettingsViewModel

@Composable
@androidx.camera.core.ExperimentalGetImage
fun SettingsUI(
    mainViewModel: MainViewModel, settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val transition = updateTransition(settingsViewModel.selectedItem.value, label = "")

    transition.AnimatedContent { settingsPages ->
        when (settingsPages) {
            SettingsPages.SETTINGS -> {
                Column(
                    modifier = Modifier.verticalScroll(settingsViewModel.scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextAuto(
                        modifier = Modifier.padding(top = 16.dp),
                        id = R.string.settings,
                        style = MaterialTheme.typography.displaySmall,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextAuto(
                        id = R.string.server_finder,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    mainViewModel.settings?.run {
                        SettingsItemSwitch(
                            title = stringResource(id = R.string.find_servers),
                            description = stringResource(id = R.string.find_servers_description),
                            icon = painterResource(id = R.drawable.device_mobile_search),
                            item = findServersAtStart,
                        ) {
                            work { save(Settings.Keys.FindServersAtStart, findServersAtStart) }
                        }

                        SettingsItemInput(
                            title = stringResource(id = R.string.ip_timeout),
                            description = stringResource(id = R.string.ip_timeout_description),
                            icon = painterResource(id = R.drawable.hourglass),
                            item = ipTimeout,
                        ) {
                            work { save(Settings.Keys.IPTimeout, ipTimeout) }
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )

                        TextAuto(
                            id = R.string.server,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!KtorService.KtorServer.isServerOn.value) SettingsItemInput(title = stringResource(
                            id = R.string.server_port
                        ),
                            description = stringResource(id = R.string.server_port_description),
                            icon = painterResource(id = R.drawable.server),
                            item = serverPortNumber,
                            onCheck = {
                                if ((it.toIntOrNull() ?: 0) in 1024..49151) true
                                else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.server_port_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    false
                                }
                            }) {
                            work { save(Settings.Keys.ServerPortNumber, serverPortNumber) }
                            KtorService.KtorServer.port = serverPortNumber.value
                        }

                        SettingsItemSwitch(
                            title = stringResource(id = R.string.server_auto_startup),
                            description = stringResource(id = R.string.server_auto_startup_description),
                            icon = painterResource(id = R.drawable.arrow_move_right),
                            item = serverAutoStartup,
                        ) {
                            work { save(Settings.Keys.ServerAutoStartup, serverAutoStartup) }
                        }

                        SettingsItemSwitch(
                            title = stringResource(id = R.string.server_shutdown_timer),
                            description = stringResource(id = R.string.server_shutdown_timer_description),
                            icon = painterResource(id = R.drawable.arrow_move_left),
                            item = mutableStateOf(millsToWait.value.isNotEmpty()),
                        ) {
                            millsToWait.value =
                                if (millsToWait.value.isNotEmpty()) ""
                                else "05:mm"
                            work { save(Settings.Keys.MillsToWait, millsToWait) }
                        }

                        AnimatedVisibility(
                            visible = millsToWait.value.isNotEmpty(),
                        ) {
                            SettingsItemTimer(
                                title = stringResource(id = R.string.timer),
                                description = stringResource(id = R.string.timer_description),
                                icon = painterResource(id = R.drawable.clock_cancel),
                                item = millsToWait,
                            ) {
                                millsToWait.value = it
                                work { save(Settings.Keys.MillsToWait, millsToWait) }
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            TextAuto(
                                id = R.string.appearance,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            SettingsItemSwitch(
                                title = stringResource(id = R.string.material_you),
                                description = stringResource(id = R.string.material_you_description),
                                icon = painterResource(id = R.drawable.palette),
                                item = isMaterialYouEnabled,
                            ) {
                                work { save(Settings.Keys.MaterialYou, isMaterialYouEnabled) }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )

                        TextAuto(
                            id = R.string.experimental,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingsItemSwitch(
                            title = stringResource(id = R.string.remove_wifi_restriction_title),
                            description = stringResource(id = R.string.remove_wifi_restriction_description),
                            icon = painterResource(id = R.drawable.wifi),
                            item = removeWifiRestriction,
                        ) {
                            work {
                                save(
                                    Settings.Keys.RemoveWifiRestriction, removeWifiRestriction
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    TextAuto(
                        id = R.string.about,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SettingsItem(title = stringResource(R.string.license),
                        icon = painterResource(id = R.drawable.file_description),
                        onClick = {
                            settingsViewModel.selectedItem.value = SettingsPages.ABOUT
                        })

                    SettingsItem(title = stringResource(R.string.open_source_licenses),
                        icon = painterResource(id = R.drawable.file_description),
                        onClick = {
                            settingsViewModel.openThirdLicenses()
                        })

                    SettingsItem(title = stringResource(R.string.privacy_policy),
                        icon = painterResource(id = R.drawable.policy_24px),
                        onClick = {
                            mainViewModel.openUrl("https://msartore.dev/projects/ares/privacy")
                        })

                    SettingsItem(title = stringResource(id = R.string.illustrations_credit),
                        icon = painterResource(id = R.drawable.draw_24px),
                        onClick = {
                            mainViewModel.openUrl("http://storyset.com/")
                        })

                    SettingsItem(title = stringResource(R.string.contribute),
                        icon = painterResource(id = R.drawable.handshake_24px),
                        onClick = {
                            mainViewModel.openUrl("https://github.com/msartore/Ares")
                        })

                    SettingsItem(title = stringResource(R.string.donate),
                        icon = painterResource(id = R.drawable.volunteer_activism_24px),
                        onClick = {
                            mainViewModel.openUrl("https://msartore.dev/donation/")
                        })

                    SettingsItem(title = stringResource(R.string.more_about_me),
                        icon = painterResource(id = R.drawable.favorite_24px),
                        onClick = {
                            mainViewModel.openUrl("https://msartore.dev/")
                        })

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TextAuto(
                            text = stringResource(id = R.string.made_by),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row {
                            TextAuto(
                                text = stringResource(id = R.string.massimiliano_sartore),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextAuto(
                            text = "Ares v${context.packageInfo().versionName}",
                            fontSize = 10.sp
                        )
                    }
                }
            }

            SettingsPages.ABOUT -> {
                BackHandler(
                    enabled = true
                ) {
                    settingsViewModel.selectedItem.value = SettingsPages.SETTINGS
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = stringResource(id = R.string.back),
                        ) {
                            settingsViewModel.selectedItem.value = SettingsPages.SETTINGS
                        }

                        TextAuto(
                            id = R.string.license,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }

                    LicenseUI()
                }
            }
        }
    }
}

enum class SettingsPages {
    SETTINGS, ABOUT
}