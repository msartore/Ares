package dev.msartore.ares.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.ui.compose.LicenseItem
import dev.msartore.ares.ui.compose.TextAuto


@Composable
fun LicenseUI() {

    val scrollState = rememberScrollState()

    Column {

        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .verticalScroll(scrollState)
        ) {
            TextAuto(
                modifier = Modifier.padding(horizontal = 16.dp),
                id = R.string.license,
            )

            LicenseItem(
                titleId = R.string.ares_title,
                text = stringResource(id = R.string.ares_description)
            )

            Divider(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            TextAuto(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.third_party_licenses),
            )

            LicenseItem(
                titleId = R.string.ktor_title,
                text = stringResource(id = R.string.ktor_descriptionTitle) + stringResource(
                    id = R.string.apache_license
                )
            )

            LicenseItem(
                titleId = R.string.kotlin_title,
                text = stringResource(id = R.string.kotlin_descriptionTitle) + stringResource(
                    id = R.string.apache_license
                )
            )

            LicenseItem(
                titleId = R.string.accompanist_title,
                text = stringResource(id = R.string.accompanist_descriptionTitle) + stringResource(
                    id = R.string.apache_license
                )
            )

            LicenseItem(
                titleId = R.string.gson_title,
                text = stringResource(id = R.string.gson_descriptionTitle) + stringResource(
                    id = R.string.apache_license
                )
            )
        }
    }
}