package me.nathanfallet.snapchatclone.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nathanfallet.snapchatclone.domain.models.CaptureRules
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.usecases.DiscardCaptureUseCase
import me.nathanfallet.snapchatclone.domain.usecases.SaveMemoryUseCase
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * The ViewModel of the camera screen: the camera settings, the capture flow (photo, video) and the snap preview.
 *
 * The camera itself (KMP Vision Camera's outputs) lives in the composition: the screen passes the actual capture
 * calls as functions, and this ViewModel decides when to call them and what to do with their results.
 *
 * @param saveMemory Saves a snap to the Memories.
 * @param discardCapture Deletes the temporary files of a discarded snap.
 * @param clock Dates the captures.
 * @param timeSource Measures the duration of recordings.
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class CameraViewModel(
    private val saveMemory: SaveMemoryUseCase,
    private val discardCapture: DiscardCaptureUseCase,
    private val clock: Clock,
    private val timeSource: TimeSource,
) : ViewModel() {
    private val state = MutableStateFlow(CameraUiState())

    /** The state of the screen. */
    val uiState: StateFlow<CameraUiState> = state.asStateFlow()

    private var filterNameJob: Job? = null
    private var messageJob: Job? = null

    // The recording in progress (see startRecording).
    private var stopRecordingFunction: (suspend () -> Unit)? = null
    private var isStopRequested = false
    private var recordingMark: TimeMark? = null
    private var recordingStartedAt: Instant? = null
    private var recordingLocation: GeoPoint? = null
    private var recordingThumbnail: ByteArray? = null
    private var recordingStartZoom: Double? = null
    private var progressJob: Job? = null

    // Camera settings

    /** Switches between the front and back cameras (double tap, or the flip button). Ignored while capturing. */
    fun flipCamera() {
        if (state.value.isBusy) return
        state.update {
            it.copy(
                facing = if (it.facing == CameraFacing.BACK) CameraFacing.FRONT else CameraFacing.BACK,
                zoom = null,
                zoomRange = null,
            )
        }
    }

    /** Turns the flash on or off. */
    fun toggleFlash() = state.update { it.copy(isFlashEnabled = !it.isFlashEnabled) }

    /** Turns the night mode (low-light boost) on or off. */
    fun toggleNightMode() = state.update { it.copy(isNightModeEnabled = !it.isNightModeEnabled) }

    /**
     * Called once the camera is configured, with its zoom capabilities (`CameraController.minZoom`, `maxZoom` and
     * its current `zoom`, which becomes the neutral `1x`).
     */
    fun onCameraReady(minZoom: Double, maxZoom: Double, zoom: Double) {
        val range = ZoomRange(min = minZoom, max = maxZoom, neutral = zoom)
        state.update { it.copy(zoomRange = range, zoom = range.clamp(zoom)) }
    }

    /** Applies a pinch of the given [scale] to the zoom. */
    fun onPinch(scale: Float) = state.update { current ->
        val range = current.zoomRange ?: return@update current
        current.copy(zoom = range.pinch(current.zoom ?: range.neutral, scale))
    }

    /**
     * Zooms while recording, when the finger holding the shutter slid up by [fraction] (`0` to `1`) of the slide
     * distance.
     */
    fun onRecordingSlide(fraction: Float) = state.update { current ->
        val range = current.zoomRange ?: return@update current
        val started = recordingStartZoom ?: return@update current
        current.copy(zoom = range.slide(started, fraction))
    }

    /** Selects the next ([forward]) or previous filter, and shows its name briefly. */
    fun swipeFilter(forward: Boolean) {
        state.update {
            it.copy(filter = if (forward) it.filter.next() else it.filter.previous(), isFilterNameVisible = true)
        }
        filterNameJob?.cancel()
        filterNameJob = viewModelScope.launch {
            delay(FILTER_NAME_DURATION)
            state.update { it.copy(isFilterNameVisible = false) }
        }
    }

    /** Reports an error of the camera session. */
    fun onCameraError(error: Throwable) = showMessage("Camera error: ${error.message ?: error}")

    // Photo

    /**
     * Takes a photo (a tap on the shutter).
     *
     * With the flash enabled, the back camera fires its flash if it [hasFlash]; the front camera turns the screen
     * white for a moment instead (screen flash), to light up the face.
     *
     * @param hasFlash Whether the current device has a flash (`CameraDevice.hasFlash`).
     * @param location Where the photo is taken, if known.
     * @param capture Captures the photo, with the device's flash if `useFlash`, and returns the path of the
     * temporary file it was saved to.
     */
    fun takePhoto(hasFlash: Boolean, location: GeoPoint?, capture: suspend (useFlash: Boolean) -> String) {
        val current = state.value
        if (current.isBusy || current.zoomRange == null) return
        val plan = flashPlan(current.facing, current.isFlashEnabled, hasFlash)
        state.update { it.copy(isCapturingPhoto = true) }
        viewModelScope.launch {
            try {
                if (plan.useScreenFlash) {
                    state.update { it.copy(isScreenFlashVisible = true) }
                    // Lets the white screen light up the face (and the auto exposure adapt) before capturing.
                    delay(SCREEN_FLASH_DURATION)
                }
                val capturedAt = clock.now()
                val path = capture(plan.useDeviceFlash)
                state.update { it.copy(isScreenFlashVisible = false) }
                val draft = MemoryDraft(
                    type = MemoryType.PHOTO,
                    media = MediaSource.File(path),
                    thumbnail = null,
                    capturedAt = capturedAt,
                    filter = Filter.NORMAL,
                    location = location,
                    duration = null,
                )
                state.update { it.copy(snap = SnapUiState(draft)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showMessage("Couldn't take the photo: ${e.message ?: e}")
            } finally {
                state.update { it.copy(isCapturingPhoto = false, isScreenFlashVisible = false) }
            }
        }
    }

    // Video

    /**
     * Starts recording a video (the shutter is held). It stops with [stopRecording], or by itself after
     * [CaptureRules.maxVideoDuration].
     *
     * @param location Where the video is recorded, if known.
     * @param starter Starts the camera's recording.
     * @param thumbnail Makes a small JPEG of the current preview, for the Memories grid.
     */
    fun startRecording(location: GeoPoint?, starter: RecordingStarter, thumbnail: () -> ByteArray?) {
        val current = state.value
        if (current.isBusy || current.zoomRange == null) return
        isStopRequested = false
        recordingLocation = location
        recordingStartZoom = current.zoom
        recordingStartedAt = clock.now()
        state.update { it.copy(recording = RecordingUiState()) }
        viewModelScope.launch {
            try {
                recordingThumbnail = thumbnail()
                val stop = starter.start(
                    onFinished = { path -> viewModelScope.launch { onRecordingFinished(path) } },
                    onFailed = { error -> viewModelScope.launch { onRecordingFailed(error) } },
                )
                recordingMark = timeSource.markNow()
                stopRecordingFunction = stop
                progressJob = launch { trackProgress() }
                // The finger was lifted while the recording was starting.
                if (isStopRequested) stopRecording()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onRecordingFailed(e)
            }
        }
    }

    /** Stops the recording (the shutter is released). The snap preview opens once the video file is complete. */
    fun stopRecording() {
        val stop = stopRecordingFunction
        if (stop == null) {
            // Still starting: stop as soon as it started.
            if (state.value.recording != null) isStopRequested = true
            return
        }
        stopRecordingFunction = null
        state.update { it.copy(recording = it.recording?.copy(isStopping = true)) }
        viewModelScope.launch {
            try {
                stop()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onRecordingFailed(e)
            }
        }
    }

    /** Updates the recording progress until it is full: the recorder then stops by itself (its max duration). */
    private suspend fun trackProgress() {
        var progress = 0f
        while (progress < 1f) {
            progress = CaptureRules.recordingProgress(recordingMark?.elapsedNow() ?: Duration.ZERO)
            state.update { current -> current.copy(recording = current.recording?.copy(progress = progress)) }
            delay(PROGRESS_INTERVAL)
        }
    }

    private fun onRecordingFinished(path: String) {
        val duration = (recordingMark?.elapsedNow() ?: Duration.ZERO).coerceAtMost(CaptureRules.maxVideoDuration)
        val draft = MemoryDraft(
            type = MemoryType.VIDEO,
            media = MediaSource.File(path),
            thumbnail = recordingThumbnail,
            capturedAt = recordingStartedAt ?: clock.now(),
            // The video output records the camera stream, not the Skia render: videos have no filter.
            filter = Filter.NORMAL,
            location = recordingLocation,
            duration = duration,
        )
        resetRecording()
        state.update { it.copy(recording = null, snap = SnapUiState(draft)) }
    }

    private fun onRecordingFailed(error: Throwable) {
        resetRecording()
        state.update { it.copy(recording = null) }
        showMessage("Couldn't record the video: ${error.message ?: error}")
    }

    private fun resetRecording() {
        progressJob?.cancel()
        progressJob = null
        stopRecordingFunction = null
        isStopRequested = false
        recordingMark = null
        recordingThumbnail = null
        recordingStartZoom = null
    }

    // Snap preview

    /** Saves the previewed snap to the Memories, and goes back to the camera. */
    fun saveSnap() {
        val snap = state.value.snap?.takeUnless { it.isSaving } ?: return
        state.update { it.copy(snap = snap.copy(isSaving = true)) }
        viewModelScope.launch {
            try {
                saveMemory(snap.draft)
                state.update { it.copy(snap = null) }
                showMessage("Saved to Memories")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(snap = snap.copy(isSaving = false)) }
                showMessage("Couldn't save: ${e.message ?: e}")
            }
        }
    }

    /** Discards the previewed snap, and goes back to the camera. */
    fun discardSnap() {
        val snap = state.value.snap?.takeUnless { it.isSaving } ?: return
        state.update { it.copy(snap = null) }
        viewModelScope.launch {
            try {
                discardCapture(snap.draft)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Only a temporary file is left behind: the system cleans its temporary directory.
                println("Couldn't delete the discarded snap: ${e.message}")
            }
        }
    }

    // Messages

    private fun showMessage(message: String) {
        state.update { it.copy(message = message) }
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            delay(MESSAGE_DURATION)
            state.update { it.copy(message = null) }
        }
    }

    /** What the flash does for a photo. */
    internal data class FlashPlan(val useDeviceFlash: Boolean, val useScreenFlash: Boolean)

    internal companion object {
        /** How long the name of a filter stays on screen after a swipe. */
        val FILTER_NAME_DURATION = 1_200.milliseconds

        /** How long the screen flash lights up before the photo is captured. */
        val SCREEN_FLASH_DURATION = 300.milliseconds

        /** How often the recording progress is updated. */
        val PROGRESS_INTERVAL = 50.milliseconds

        /** How long a message stays on screen. */
        val MESSAGE_DURATION = 2_500.milliseconds

        /**
         * The flash of a photo: the device's flash on the back camera if it has one, the screen flash on the front
         * camera (front cameras have no flash).
         */
        fun flashPlan(facing: CameraFacing, isFlashEnabled: Boolean, hasFlash: Boolean) = FlashPlan(
            useDeviceFlash = isFlashEnabled && facing == CameraFacing.BACK && hasFlash,
            useScreenFlash = isFlashEnabled && facing == CameraFacing.FRONT,
        )
    }
}
