package me.nathanfallet.snapchatclone.presentation.memories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import okio.Path.Companion.toPath
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.presentation.components.CircleIconButton
import me.nathanfallet.snapchatclone.presentation.components.MessageBanner
import me.nathanfallet.snapchatclone.presentation.components.PlatformBackHandler
import me.nathanfallet.snapchatclone.presentation.theme.Accent
import me.nathanfallet.snapchatclone.presentation.theme.AppIcons

/**
 * The Memories: a grid of the saved snaps, newest first and grouped by month, opening a full-screen viewer.
 *
 * @param viewModel The ViewModel of the screen.
 * @param onClose Goes back to the camera.
 * @param modifier The modifier of the screen.
 */
@Composable
fun MemoriesScreen(viewModel: MemoriesViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var viewedMemoryId by remember { mutableStateOf<String?>(null) }
    PlatformBackHandler(enabled = viewedMemoryId == null, onBack = onClose)

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                CircleIconButton(AppIcons.ChevronDown, contentDescription = "Back to the camera", onClick = onClose)
                Text(
                    "Memories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(44.dp))
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }

                state.memories.isEmpty() -> EmptyMemories()

                else -> MemoriesGrid(state.sections, onOpen = { viewedMemoryId = it.id })
            }
        }

        val viewedIndex = state.memories.indexOfFirst { it.id == viewedMemoryId }
        AnimatedVisibility(visible = viewedIndex >= 0, enter = fadeIn(), exit = fadeOut()) {
            if (viewedIndex >= 0) {
                MemoryViewer(
                    memories = state.memories,
                    initialIndex = viewedIndex,
                    formattedDate = viewModel::formattedDate,
                    onDelete = viewModel::delete,
                    onClose = { viewedMemoryId = null },
                )
            }
        }

        MessageBanner(
            message = state.message,
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun MemoriesGrid(sections: List<MemorySection>, onOpen: (Memory) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        sections.forEach { section ->
            item(key = "header-${section.title}", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }
            items(section.memories, key = { it.id }) { memory ->
                MemoryCell(memory, onClick = { onOpen(memory) })
            }
        }
    }
}

@Composable
private fun MemoryCell(memory: Memory, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(CELL_ASPECT_RATIO)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = (memory.thumbnailPath ?: memory.mediaPath).toPath(),
            contentDescription = if (memory.type == MemoryType.VIDEO) "Video" else "Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        val duration = memory.duration
        if (memory.type == MemoryType.VIDEO && duration != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(start = 4.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
            ) {
                Icon(AppIcons.Play, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(formatDuration(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EmptyMemories() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Icon(AppIcons.Memories, contentDescription = null, tint = Accent, modifier = Modifier.size(56.dp))
        Text("No Memories yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Take a snap and tap \"Save to Memories\": it stays on this device only.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private const val COLUMNS = 3
private const val CELL_ASPECT_RATIO = 9f / 16f
