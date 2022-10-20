
package dev.msartore.ares.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import dev.msartore.ares.R
import dev.msartore.ares.ui.compose.Dialog

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(
    permissionState: MultiplePermissionsState?,
    requestStringId: Int,
    settingsStringId: Int,
    navigateToSettingsScreen: () -> Unit,
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: () -> Unit
) {

    if (permissionState?.allPermissionsGranted == true || permissionState == null) {
        onPermissionGranted()
    }
    else {
        val dialogStatus = remember { mutableStateOf(permissionState.permissions.none { it.status.shouldShowRationale }) }

        Dialog(
            title = stringResource(R.string.permission_request),

            text = stringResource(requestStringId),
            closeOnClick = false,
            status = dialogStatus,
            onCancel = {
                onPermissionDenied()
            },
            onConfirm = {
                dialogStatus.value = false
                permissionState.permissions.forEach {
                    it.launchPermissionRequest()
                }
            }
        )
    }

    if (permissionState?.permissions?.any { it.status.shouldShowRationale } == true)
        DialogPermissionRejected(
            navigateToSettingsScreen = navigateToSettingsScreen,
            onCancel = onPermissionDenied,
            settingsStringId = settingsStringId
        )
}

@Composable
fun DialogPermissionRejected(
    navigateToSettingsScreen: () -> Unit,
    settingsStringId: Int,
    onCancel: () -> Unit = {},
) {
    val dialogStatus = remember { mutableStateOf(true) }

    Dialog(
        title = stringResource(id = R.string.permission_rejected),
        text = stringResource(settingsStringId),
        status = dialogStatus,
        confirmText = stringResource(id = R.string.open_settings),
        onCancel = onCancel,
        closeOnClick = false,
        onConfirm = {
            navigateToSettingsScreen()
        }
    )
}