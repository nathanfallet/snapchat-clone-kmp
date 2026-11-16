package me.nathanfallet.snapchatclone.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/** Handles the system back with the [BackHandler] of AndroidX Activity. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)
