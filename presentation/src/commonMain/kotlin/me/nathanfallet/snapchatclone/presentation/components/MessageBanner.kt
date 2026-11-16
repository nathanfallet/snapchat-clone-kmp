package me.nathanfallet.snapchatclone.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A short message in a pill (e.g. "Saved to Memories"), fading in and out.
 *
 * @param message The message, or `null` to hide it.
 * @param modifier The modifier of the banner.
 */
@Composable
fun MessageBanner(message: String?, modifier: Modifier = Modifier) {
    // Keeps the last message while it fades out.
    var lastMessage by remember { mutableStateOf(message) }
    if (message != null) lastMessage = message
    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Text(
            lastMessage.orEmpty(),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}
