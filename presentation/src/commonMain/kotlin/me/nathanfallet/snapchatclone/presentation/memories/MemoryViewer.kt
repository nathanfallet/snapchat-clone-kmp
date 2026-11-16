package me.nathanfallet.snapchatclone.presentation.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import okio.Path.Companion.toPath
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.presentation.components.CircleIconButton
import me.nathanfallet.snapchatclone.presentation.components.PlatformBackHandler
import me.nathanfallet.snapchatclone.presentation.components.VideoPlayer
import me.nathanfallet.snapchatclone.presentation.theme.AppIcons
import me.nathanfallet.snapchatclone.presentation.theme.Recording

/**
 * Shows the [memories] full screen, swiping between them, starting at [initialIndex]. Videos play in a loop while
 * they are on screen.
 *
 * @param memories Every memory, newest first.
 * @param initialIndex The index of the memory to show first.
 * @param formattedDate Formats the capture date of a memory.
 * @param onDelete Deletes a memory (after a confirmation).
 * @param onClose Goes back to the grid.
 */
@Composable
fun MemoryViewer(
    memories: List<Memory>,
    initialIndex: Int,
    formattedDate: (Memory) -> String,
    onDelete: (Memory) -> Unit,
    onClose: () -> Unit,
) {
    PlatformBackHandler(enabled = true, onBack = onClose)
    val pagerState = rememberPagerState(initialPage = initialIndex) { memories.size }
    var memoryToDelete by remember { mutableStateOf<Memory?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, key = { memories[it].id }, modifier = Modifier.fillMaxSize()) { page ->
            val memory = memories[page]
            if (memory.type == MemoryType.VIDEO) {
                VideoPlayer(
                    path = memory.mediaPath,
                    isPlaying = pagerState.currentPage == page,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AsyncImage(
                    model = memory.mediaPath.toPath(),
                    contentDescription = "Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        val current = memories.getOrNull(pagerState.currentPage)
        if (current != null) {
            MemoryDetails(
                memory = current,
                date = formattedDate(current),
                position = "${pagerState.currentPage + 1} / ${memories.size}",
                onClose = onClose,
                onDelete = { memoryToDelete = current },
            )
        }
    }

    memoryToDelete?.let { memory ->
        AlertDialog(
            onDismissRequest = { memoryToDelete = null },
            title = { Text(if (memory.type == MemoryType.VIDEO) "Delete this video?" else "Delete this photo?") },
            text = { Text("It will be deleted from this device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    memoryToDelete = null
                    if (memories.size <= 1) onClose()
                    onDelete(memory)
                }) { Text("Delete", color = Recording, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { memoryToDelete = null }) { Text("Cancel") } },
        )
    }
}

/** The date, the location and the actions of the shown [memory], over a gradient at the top and the bottom. */
@Composable
private fun MemoryDetails(memory: Memory, date: String, position: String, onClose: () -> Unit, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
                .safeDrawingPadding()
                .padding(16.dp),
        ) {
            CircleIconButton(AppIcons.Close, contentDescription = "Close", onClick = onClose)
            Column(modifier = Modifier.weight(1f)) {
                Text(date, color = Color.White, fontWeight = FontWeight.Bold)
                memory.location?.let { location ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            AppIcons.Location,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(formatLocation(location), color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text(position, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                .safeDrawingPadding()
                .padding(16.dp),
        ) {
            val details = buildList {
                if (memory.filter != Filter.NORMAL) add(memory.filter.displayName)
                memory.duration?.let { add(formatDuration(it)) }
            }.joinToString(" · ")
            Text(
                details,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            CircleIconButton(AppIcons.Delete, contentDescription = "Delete", onClick = onDelete)
        }
    }
}
