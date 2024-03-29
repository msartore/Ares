package dev.msartore.ares.ui.compose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextAuto(
    modifier: Modifier = Modifier,
    text: String? = null,
    id: Int? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = 17.sp,
    maxLines: Int = 2,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    if (id != null) Text(
        modifier = modifier,
        text = stringResource(id = id),
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        maxLines = maxLines,
        fontSize = fontSize,
        overflow = TextOverflow.Ellipsis,
        style = style
    )
    else Text(
        modifier = modifier,
        text = text.toString(),
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style
    )
}