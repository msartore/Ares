package dev.msartore.ares.ui.views

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.msartore.ares.R
import dev.msartore.ares.models.Settings
import dev.msartore.ares.ui.compose.*
import dev.msartore.ares.utils.work

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsUI(
    openUrl: (String) -> Unit,
    settings: Settings
) {

    val scrollState = rememberScrollState()
    var info: PackageInfo?
    val selectedItem = remember { mutableStateOf(SettingsPages.SETTINGS) }
    val transition = updateTransition(selectedItem.value, label = "")
    val backAction = {
        selectedItem.value = SettingsPages.SETTINGS
    }

    LocalContext.current.apply {
        info =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
    }

    BackHandler(
        enabled = selectedItem.value == SettingsPages.ABOUT
    ) {
        backAction()
    }

    transition.AnimatedContent {
        Column {

            if (it == SettingsPages.ABOUT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back_24px),
                        contentDescription = stringResource(id = R.string.back),
                    ) {
                        backAction()
                    }

                    TextAuto(
                        id = R.string.licenses,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            when(it) {
                SettingsPages.SETTINGS -> {

                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextAuto(
                            modifier = Modifier
                                .padding(top = 16.dp),
                            id = R.string.settings,
                            style = MaterialTheme.typography.displaySmall,
                            interactable = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        TextAuto(
                            id = R.string.wifi_scan,
                            style = MaterialTheme.typography.labelLarge,
                            interactable = true
                        )

                        SettingsItemSwitch(
                            title = stringResource(id = R.string.find_servers_start_app),
                            icon = painterResource(id = R.drawable.wifi_find_24px),
                            item = settings.findServersAtStart,
                        ) {
                            work { settings.save() }
                        }

                        SettingsItemInput(
                            title = stringResource(id = R.string.ip_timeout),
                            icon = painterResource(id = R.drawable.timer_24px),
                            item = settings.ipTimeout,
                        ) {
                            work { settings.save() }
                        }

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )

                        TextAuto(
                            id = R.string.about,
                            style = MaterialTheme.typography.labelLarge,
                            interactable = true
                        )

                        SettingsItem(
                            title = stringResource(R.string.licenses),
                            icon = painterResource(id = R.drawable.description_24px),
                            onClick = {
                                selectedItem.value = SettingsPages.ABOUT
                            }
                        )

                        SettingsItem(
                            title = stringResource(R.string.privacy_policy),
                            icon = painterResource(id = R.drawable.policy_24px),
                            onClick = {
                                openUrl("https://msartore.dev/ares/privacy/")
                            }
                        )
                        
                        SettingsItem(
                            title = stringResource(id = R.string.illustrations_credit),
                            icon = painterResource(id = R.drawable.draw_24px),
                            onClick = {
                                openUrl("http://storyset.com/")
                            }
                        )

                        SettingsItem(
                            title = stringResource(R.string.contribute),
                            icon = painterResource(id = R.drawable.handshake_24px),
                            onClick = {
                                openUrl("https://github.com/msartore/Ares")
                            }
                        )

                        SettingsItem(
                            title = stringResource(R.string.donate),
                            icon = painterResource(id = R.drawable.volunteer_activism_24px),
                            onClick = {
                                openUrl("https://msartore.dev/donation/")
                            }
                        )

                        SettingsItem(
                            title = stringResource(R.string.more_about_me),
                            icon = painterResource(id = R.drawable.favorite_24px),
                            onClick = {
                                openUrl("https://msartore.dev/#projects")
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextAuto(
                                text = "Ares v${info?.versionName}",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                SettingsPages.ABOUT -> LicenseUI()
            }
        }
    }
}

enum class SettingsPages {
    SETTINGS,
    ABOUT
}