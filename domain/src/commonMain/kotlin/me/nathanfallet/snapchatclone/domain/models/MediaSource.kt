package me.nathanfallet.snapchatclone.domain.models

/**
 * Where the media of a [MemoryDraft] currently is, before it is saved to the Memories.
 */
sealed interface MediaSource {
    /**
     * Encoded media in memory, e.g. a filtered photo.
     *
     * @property bytes The encoded file content.
     * @property extension The file extension, without the dot (e.g. `jpg`).
     */
    class Bytes(val bytes: ByteArray, val extension: String) : MediaSource

    /**
     * A temporary file written by the camera, e.g. a recorded video. Saving the draft moves it.
     *
     * @property path The absolute path of the file.
     */
    data class File(val path: String) : MediaSource
}
