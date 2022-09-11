package dev.msartore.ares.ui.compose

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
import dev.msartore.ares.ui.compose.basic.TextAuto


@Composable
fun LicenseUI() {

    val scrollState = rememberScrollState()

    Column {

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            TextAuto(
                modifier = Modifier.padding(horizontal = 16.dp),
                id = R.string.license
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(35.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {

                TextAuto(
                    id = R.string.ares_title,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                TextAuto(id = R.string.ares_description)
            }

            Divider(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            TextAuto(
                modifier = Modifier.padding(horizontal = 16.dp),
                id = R.string.third_party_licenses
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(35.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {

                TextAuto(
                    id = R.string.ktor_title,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                TextAuto(id = R.string.apache_license)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(35.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {

                TextAuto(
                    id = R.string.kotlin_title,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                TextAuto(
                    text = stringResource(id = R.string.kotlin_descriptionTitle) + stringResource(
                        id = R.string.apache_license
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(35.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {

                TextAuto(
                    id = R.string.accompanist_title,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                TextAuto(
                    text = stringResource(id = R.string.accompanist_descriptionTitle) + stringResource(
                        id = R.string.apache_license
                    ),
                )
            }
        }
    }
}