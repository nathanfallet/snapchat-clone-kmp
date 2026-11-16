package me.nathanfallet.snapchatclone.domain.repositories

/**
 * Manages the temporary files the camera writes (photos before they are filtered, videos before they are saved).
 */
interface CapturesRepository {
    /** Reads the temporary capture at [path]. */
    suspend fun readCapture(path: String): ByteArray

    /** Deletes the temporary capture at [path]. Does nothing if it does not exist anymore. */
    suspend fun deleteCapture(path: String)
}
