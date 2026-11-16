package me.nathanfallet.snapchatclone.presentation.components

import androidx.compose.runtime.Composable

/**
 * Handles the system back gesture while [enabled] (Android's back button). iOS has no system back: nothing to do.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
