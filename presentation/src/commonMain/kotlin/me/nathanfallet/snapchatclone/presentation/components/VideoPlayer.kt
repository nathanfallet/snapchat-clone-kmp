package me.nathanfallet.snapchatclone.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Plays the video file at [path] in a loop, filling its bounds (cropped), without controls, like a snap.
 *
 * It uses Media3's ExoPlayer on Android and AVPlayer on iOS.
 *
 * @param path The absolute path of the video file.
 * @param isPlaying Whether the video plays, or is paused (e.g. when it is not the visible page of a pager).
 * @param modifier The modifier of the player.
 */
@Composable
expect fun VideoPlayer(path: String, isPlaying: Boolean, modifier: Modifier = Modifier)
