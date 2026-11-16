package me.nathanfallet.snapchatclone.presentation.camera

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.nathanfallet.snapchatclone.domain.models.ScannedCode
import me.nathanfallet.snapchatclone.presentation.theme.Accent
import me.nathanfallet.snapchatclone.presentation.theme.AppIcons

/**
 * What the long press scan shows: a pulsing frame while scanning, then a card with the scanned code.
 *
 * @param scan The state of the scan.
 * @param onDismiss Closes the card.
 * @param modifier The modifier of the overlay.
 */
@Composable
fun ScanOverlay(scan: ScanUiState, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (scan) {
            ScanUiState.Scanning -> ScanningFrame(modifier = Modifier.align(Alignment.Center))
            is ScanUiState.Found -> ScannedCodeCard(
                code = scan.code,
                onDismiss = onDismiss,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 140.dp),
            )

            ScanUiState.Idle, ScanUiState.NotFound -> Unit
        }
    }
}

/** A pulsing square frame in the middle of the screen, with a hint. */
@Composable
private fun ScanningFrame(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Canvas(modifier = Modifier.size(FRAME_SIZE)) {
            val side = size.minDimension * scale
            val topLeft = Offset((size.width - side) / 2, (size.height - side) / 2)
            drawRoundRect(
                color = Accent,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(24.dp.toPx()),
                style = Stroke(width = 4.dp.toPx()),
            )
        }
        Text(
            "Scanning…",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

/** A card with the value of the scanned [code], and a button opening it if it is a link. */
@Composable
private fun ScannedCodeCard(code: ScannedCode, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val link = code.link
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            if (link != null) "Link found" else "QR code found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            code.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            if (link != null) {
                Button(onClick = {
                    uriHandler.openUri(link)
                    onDismiss()
                }) {
                    Icon(AppIcons.OpenLink, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Open", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

private const val PULSE_MILLIS = 700
private val FRAME_SIZE = 240.dp
