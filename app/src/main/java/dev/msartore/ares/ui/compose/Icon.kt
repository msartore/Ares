package dev.msartore.ares.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun Icon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    id: Int? = null,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    shadowEnabled: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {

    var localModifier = modifier

    if (shadowEnabled)
        localModifier = localModifier.shadow(shape = RoundedCornerShape(16.dp), elevation = 50.dp)

    if (onClick != null)
        localModifier = localModifier
            .clip(RoundedCornerShape(35.dp))
            .clickable { onClick.invoke() }
            .padding(8.dp)

    when {
        imageVector != null -> Icon(
            modifier = localModifier,
            tint = tint,
            imageVector = imageVector,
            contentDescription = contentDescription
        )
        painter != null -> Icon(
            modifier = localModifier,
            tint = tint,
            painter = painter,
            contentDescription = contentDescription
        )
        id != null -> Icon(
            modifier = localModifier,
            tint = tint,
            painter = painterResource(id),
            contentDescription = contentDescription
        )
    }
}