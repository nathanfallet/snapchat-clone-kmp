package me.nathanfallet.snapchatclone.presentation.camera

/**
 * The `enableLowLightBoost` value to give the camera controller.
 *
 * A device without low-light boost refuses any value, even `false`, so it gets `null` (leave it as it is).
 *
 * @param isNightModeEnabled Whether the user turned the night mode on.
 * @param isSupported Whether the current device supports low-light boost, `null` while it is not known yet.
 */
fun lowLightBoostSetting(isNightModeEnabled: Boolean, isSupported: Boolean?): Boolean? =
    if (isSupported == true) isNightModeEnabled else null
