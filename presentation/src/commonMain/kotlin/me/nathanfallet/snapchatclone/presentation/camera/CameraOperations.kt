package me.nathanfallet.snapchatclone.presentation.camera

import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.visioncamera.Location
import me.nathanfallet.visioncamera.skia.compose.SkiaCamera
import me.nathanfallet.visioncamera.skia.compose.SkiaCameraRef
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

/** Takes a small JPEG of what the [SkiaCamera] currently shows, for the Memories grid. */
internal fun SkiaCameraRef.snapshotThumbnail(): ByteArray? {
    val snapshot = takeSnapshot() ?: return null
    return try {
        encodeThumbnail(snapshot)
    } finally {
        snapshot.close()
    }
}

/** Converts a KMP Vision Camera [Location] to a domain [GeoPoint], or `null` if it is not a valid position. */
internal fun Location.toGeoPoint(): GeoPoint? = GeoPoint.orNull(latitude, longitude)

/** Runs [block], logging its failures (but not cancellations): focus and zoom fail while the camera reconfigures. */
@Suppress("TooGenericExceptionCaught")
internal suspend fun attempt(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        println("Camera operation failed: ${e.message}")
    }
}

/** Whether a displayed zoom (`1.0` for the neutral zoom) is worth showing to the user. */
internal fun Double.isZoomedIn() = this > ZOOM_LABEL_THRESHOLD

/** Formats a zoom with one decimal (e.g. `2.5`). */
internal fun Double.formatZoom(): String {
    val tenths = (this * TENTHS).roundToInt()
    return "${tenths / TENTHS}.${tenths % TENTHS}"
}

private const val ZOOM_LABEL_THRESHOLD = 1.05
private const val TENTHS = 10
