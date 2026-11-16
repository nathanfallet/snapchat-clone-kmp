package me.nathanfallet.snapchatclone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.nathanfallet.snapchatclone.presentation.theme.Scrim

/**
 * A round, translucent icon button, drawn over the camera or a snap.
 *
 * @param icon The icon.
 * @param contentDescription What the button does, for accessibility.
 * @param onClick Called when the button is tapped.
 * @param modifier The modifier of the button.
 * @param tint The color of the icon (e.g. the accent when a toggle is on).
 * @param enabled Whether the button can be tapped.
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    enabled: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Scrim)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp),
        )
    }
}
