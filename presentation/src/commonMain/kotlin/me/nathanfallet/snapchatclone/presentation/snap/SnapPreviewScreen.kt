package me.nathanfallet.snapchatclone.presentation.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import okio.Path.Companion.toPath
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.presentation.camera.SnapUiState
import me.nathanfallet.snapchatclone.presentation.components.CircleIconButton
import me.nathanfallet.snapchatclone.presentation.components.PlatformBackHandler
import me.nathanfallet.snapchatclone.presentation.components.VideoPlayer
import me.nathanfallet.snapchatclone.presentation.theme.AppIcons
import me.nathanfallet.snapchatclone.presentation.theme.Scrim

/**
 * The preview of a captured snap: the (filtered) photo, or the video playing in a loop, with buttons to save it to
 * the Memories or discard it.
 *
 * @param snap The captured snap.
 * @param onSave Saves it to the Memories.
 * @param onDiscard Discards it.
 * @param modifier The modifier of the screen.
 */
@Composable
fun SnapPreviewScreen(snap: SnapUiState, onSave: () -> Unit, onDiscard: () -> Unit, modifier: Modifier = Modifier) {
    PlatformBackHandler(enabled = true, onBack = onDiscard)
    val draft = snap.draft
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val media = draft.media
        when {
            media is MediaSource.File && draft.type == MemoryType.VIDEO -> VideoPlayer(
                path = media.path,
                isPlaying = true,
                modifier = Modifier.fillMaxSize(),
            )

            media is MediaSource.File -> AsyncImage(
                model = media.path.toPath(),
                contentDescription = "Captured photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            media is MediaSource.Bytes -> AsyncImage(
                model = media.bytes,
                contentDescription = "Captured photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            CircleIconButton(
                icon = AppIcons.Close,
                contentDescription = "Discard",
                onClick = onDiscard,
                enabled = !snap.isSaving,
                modifier = Modifier.align(Alignment.TopStart),
            )
            if (draft.filter != Filter.NORMAL) {
                Text(
                    draft.filter.displayName,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .background(Scrim, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            ) {
                Button(
                    onClick = onSave,
                    enabled = !snap.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (snap.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Icon(AppIcons.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "Save to Memories",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
