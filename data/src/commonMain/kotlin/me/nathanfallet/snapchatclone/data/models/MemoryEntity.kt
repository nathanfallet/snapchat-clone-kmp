package me.nathanfallet.snapchatclone.data.models

import kotlinx.serialization.Serializable

/**
 * A memory as stored in the JSON index. File names are relative to the memories' directory, so the index stays valid
 * if the app's directory moves (e.g. iOS changes the container path between installs).
 *
 * @property id The unique identifier of the memory.
 * @property type The name of its `MemoryType`.
 * @property capturedAt The capture date, in milliseconds since the Unix epoch.
 * @property mediaFile The name of the media file.
 * @property thumbnailFile The name of the thumbnail file, if any.
 * @property filter The name of its `Filter`.
 * @property latitude The latitude where it was captured, if known.
 * @property longitude The longitude where it was captured, if known.
 * @property durationMillis The duration of a video, in milliseconds.
 */
@Serializable
data class MemoryEntity(
    val id: String,
    val type: String,
    val capturedAt: Long,
    val mediaFile: String,
    val thumbnailFile: String? = null,
    val filter: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val durationMillis: Long? = null,
)

/**
 * The JSON index of the memories (`index.json`).
 *
 * @property version The version of the index format, for future migrations.
 * @property memories The stored memories.
 */
@Serializable
data class MemoriesIndex(
    val version: Int = CURRENT_VERSION,
    val memories: List<MemoryEntity> = emptyList(),
) {
    /** The versions of the index format. */
    companion object {
        /** The version of the index format written by this version of the app. */
        const val CURRENT_VERSION: Int = 1
    }
}
