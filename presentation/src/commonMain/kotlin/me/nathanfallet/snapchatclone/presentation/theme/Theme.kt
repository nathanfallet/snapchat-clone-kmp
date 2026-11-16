package me.nathanfallet.snapchatclone.presentation.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/** The accent of the app: a bright yellow, for the shutter ring, buttons and highlights. */
val Accent = Color(0xFFFFE53B)

/** The color of recording: the ring around the shutter while a video is recorded. */
val Recording = Color(0xFFFF3B5C)

/** A translucent black, behind controls drawn over the camera. */
val Scrim = Color(0x66000000)

/** The colors of the app. */
internal val AppColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Recording,
)

/**
 * The color of the text and icons that don't choose one. Outside of a `Surface`, Compose draws them in black by
 * default, which is invisible on the black background of the app.
 */
internal val DefaultContentColor = AppColorScheme.onBackground

/**
 * The theme of the app: dark, like a camera app, with a yellow accent.
 */
@Composable
fun SnapCloneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme) {
        CompositionLocalProvider(LocalContentColor provides DefaultContentColor, content = content)
    }
}
