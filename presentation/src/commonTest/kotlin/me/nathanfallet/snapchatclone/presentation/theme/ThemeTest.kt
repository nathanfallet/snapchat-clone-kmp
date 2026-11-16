package me.nathanfallet.snapchatclone.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

class ThemeTest {

    /** The WCAG contrast ratio between two colors, from 1 (same luminance) to 21 (black and white). */
    private fun contrast(a: Color, b: Color): Float {
        val lighter = maxOf(a.luminance(), b.luminance())
        val darker = minOf(a.luminance(), b.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    @Test
    fun defaultContentIsReadableOnTheBackground() {
        // The Memories title and month headers use the default content color: they were black on black.
        assertTrue(contrast(DefaultContentColor, AppColorScheme.background) >= 4.5f)
        assertTrue(contrast(DefaultContentColor, AppColorScheme.surface) >= 4.5f)
    }
}
