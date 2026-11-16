package me.nathanfallet.snapchatclone.presentation.components

import androidx.compose.runtime.Composable

/** Does nothing: iOS has no system back button (screens have their own close buttons). */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
