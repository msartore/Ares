package dev.msartore.ares.ui.views

import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraControl
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.BarcodeImageAnalyzer
import dev.msartore.ares.ui.compose.CameraPreview
import dev.msartore.ares.ui.compose.Icon

@ExperimentalGetImage
@Composable
fun CameraUI(
    visibility: MutableState<Boolean>,
    onQRFound: ((String) -> Unit)? = null,
) {

    val isFlashEnabled = remember { mutableStateOf(false) }
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }

    BackHandler(true) {
        visibility.value = false
    }

    Box {
        CameraPreview(
            analyzer = BarcodeImageAnalyzer(onQRFound),
            cameraControl = cameraControl,
            isFlashEnabled = isFlashEnabled
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(50.dp)),
                painter = painterResource(id = R.drawable.arrow_back_24px),
                contentDescription = stringResource(id = R.string.back),
            ) {
                visibility.value = false
            }

            Icon(
                modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(50.dp)),
                painter = painterResource(id =
                    if(isFlashEnabled.value) R.drawable.flash_on_24px else R.drawable.flash_off_24px
                ),
                contentDescription = stringResource(id = R.string.back),
            ) {
                isFlashEnabled.value = !isFlashEnabled.value
                cameraControl.value?.enableTorch(isFlashEnabled.value)
            }
        }
    }
}