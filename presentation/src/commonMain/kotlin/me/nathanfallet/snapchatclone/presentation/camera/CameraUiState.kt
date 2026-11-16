package me.nathanfallet.snapchatclone.presentation.camera

import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.ScannedCode

/** Which camera is used. */
enum class CameraFacing {
    /** The selfie camera, mirrored. */
    FRONT,

    /** The main camera. */
    BACK,
}

/**
 * A video being recorded.
 *
 * @property progress How much of the maximum duration was recorded, between `0` and `1`.
 * @property isStopping Whether the recording was asked to stop and is finishing its file.
 */
data class RecordingUiState(val progress: Float = 0f, val isStopping: Boolean = false)

/** The QR code scan started by a long press on the preview. */
sealed interface ScanUiState {
    /** No scan. */
    data object Idle : ScanUiState

    /** The camera frames are scanned for a code. */
    data object Scanning : ScanUiState

    /**
     * A code was found.
     *
     * @property code The scanned code.
     */
    data class Found(val code: ScannedCode) : ScanUiState

    /** No code was found in time. */
    data object NotFound : ScanUiState
}

/**
 * A captured snap, shown in the preview screen.
 *
 * @property draft The captured media, to save or discard.
 * @property isSaving Whether it is being saved to the Memories.
 */
data class SnapUiState(val draft: MemoryDraft, val isSaving: Boolean = false)

/**
 * The state of the camera screen.
 *
 * @property facing The camera in use.
 * @property isFlashEnabled Whether the flash fires (the screen flash on the front camera).
 * @property isNightModeEnabled Whether the low-light boost is requested.
 * @property zoom The zoom to apply, or `null` until the camera is ready.
 * @property zoomRange The zoom the camera supports, or `null` until it is ready.
 * @property filter The selected filter.
 * @property isFilterNameVisible Whether the name of the [filter] is shown (briefly, after a swipe).
 * @property isCapturingPhoto Whether a photo is being captured or processed.
 * @property isScreenFlashVisible Whether the white screen flash covers the screen.
 * @property recording The video being recorded, if any.
 * @property scan The QR code scan.
 * @property snap The captured snap to preview, if any.
 * @property message A short message to show the user, if any.
 */
data class CameraUiState(
    val facing: CameraFacing = CameraFacing.BACK,
    val isFlashEnabled: Boolean = false,
    val isNightModeEnabled: Boolean = false,
    val zoom: Double? = null,
    val zoomRange: ZoomRange? = null,
    val filter: Filter = Filter.NORMAL,
    val isFilterNameVisible: Boolean = false,
    val isCapturingPhoto: Boolean = false,
    val isScreenFlashVisible: Boolean = false,
    val recording: RecordingUiState? = null,
    val scan: ScanUiState = ScanUiState.Idle,
    val snap: SnapUiState? = null,
    val message: String? = null,
) {
    /** Whether a capture is in progress or being previewed, so no other one can start. */
    val isBusy: Boolean get() = isCapturingPhoto || recording != null || snap != null

    /** The zoom to display (`1.0` for the neutral zoom), or `null` until the camera is ready. */
    val displayedZoom: Double? get() = zoom?.let { zoomRange?.displayed(it) }

    /**
     * The filter drawn on the preview: none while recording, since videos are recorded without filter (what is on
     * screen is what is recorded).
     */
    val previewFilter: Filter get() = if (recording != null) Filter.NORMAL else filter
}
