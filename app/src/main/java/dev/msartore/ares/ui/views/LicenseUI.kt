package dev.msartore.ares.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.ui.compose.LicenseItem
import dev.msartore.ares.ui.compose.TextAuto


@Composable
fun LicenseUI() {

    val verticalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .verticalScroll(verticalScrollState)
    ) {
        TextAuto(
            modifier = Modifier.padding(horizontal = 16.dp),
            id = R.string.license,
        )

        LicenseItem(
            titleId = R.string.ares_title,
            text = stringResource(id = R.string.ares_description)
        )
    }
}