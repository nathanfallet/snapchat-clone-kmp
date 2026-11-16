package me.nathanfallet.snapchatclone.domain.usecases

/**
 * Reads a temporary file the camera wrote, e.g. a photo to filter.
 */
interface ReadCaptureUseCase {
    /** Returns the content of the capture at [path]. */
    suspend operator fun invoke(path: String): ByteArray
}
