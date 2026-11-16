package me.nathanfallet.snapchatclone.presentation.camera

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.nathanfallet.snapchatclone.domain.models.CaptureRules
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.usecases.DiscardCaptureUseCase
import me.nathanfallet.snapchatclone.domain.usecases.ReadCaptureUseCase
import me.nathanfallet.snapchatclone.domain.usecases.SaveMemoryUseCase
import me.nathanfallet.snapchatclone.presentation.filters.PhotoFilterRenderer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val saved = mutableListOf<MemoryDraft>()
    private val discarded = mutableListOf<MemoryDraft>()
    private val now = Instant.fromEpochMilliseconds(1_700_000_000_000)

    private val saveMemory = object : SaveMemoryUseCase {
        override suspend fun invoke(draft: MemoryDraft): Memory {
            saved += draft
            return Memory(
                id = "id",
                type = draft.type,
                capturedAt = draft.capturedAt,
                mediaPath = "/m/id",
                thumbnailPath = null,
                filter = draft.filter,
                location = draft.location,
                duration = draft.duration,
            )
        }
    }
    private val discardCapture = object : DiscardCaptureUseCase {
        override suspend fun invoke(draft: MemoryDraft) {
            discarded += draft
        }

        override suspend fun invoke(path: String) {
            deletedCaptures += path
        }
    }
    private val captures = mutableMapOf("/tmp/photo.jpg" to byteArrayOf(1, 2, 3), "/tmp/selfie.jpg" to byteArrayOf(4))
    private val deletedCaptures = mutableListOf<String>()
    private val readCapture = object : ReadCaptureUseCase {
        override suspend fun invoke(path: String): ByteArray = captures.getValue(path)
    }
    private val renderedFilters = mutableListOf<Filter>()
    private val photoFilterRenderer = PhotoFilterRenderer { encoded, filter ->
        renderedFilters += filter
        encoded + byteArrayOf(filter.ordinal.toByte())
    }
    private val clock = object : Clock {
        override fun now() = now
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel() = CameraViewModel(
        saveMemory = saveMemory,
        readCapture = readCapture,
        discardCapture = discardCapture,
        photoFilterRenderer = photoFilterRenderer,
        clock = clock,
        timeSource = testScheduler.timeSource,
    )

    private fun TestScope.readyViewModel() =
        viewModel().apply { onCameraReady(minZoom = 1.0, maxZoom = 20.0, zoom = 1.0) }

    @Test
    fun flashUsesTheDeviceFlashOnTheBackAndTheScreenOnTheFront() {
        assertEquals(
            CameraViewModel.FlashPlan(useDeviceFlash = true, useScreenFlash = false),
            CameraViewModel.flashPlan(CameraFacing.BACK, isFlashEnabled = true, hasFlash = true),
        )
        assertEquals(
            CameraViewModel.FlashPlan(useDeviceFlash = false, useScreenFlash = false),
            CameraViewModel.flashPlan(CameraFacing.BACK, isFlashEnabled = true, hasFlash = false),
        )
        assertEquals(
            CameraViewModel.FlashPlan(useDeviceFlash = false, useScreenFlash = true),
            CameraViewModel.flashPlan(CameraFacing.FRONT, isFlashEnabled = true, hasFlash = false),
        )
        assertEquals(
            CameraViewModel.FlashPlan(useDeviceFlash = false, useScreenFlash = false),
            CameraViewModel.flashPlan(CameraFacing.FRONT, isFlashEnabled = false, hasFlash = true),
        )
    }

    @Test
    fun aPhotoOpensTheSnapPreview() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        var usedFlash: Boolean? = null
        viewModel.toggleFlash()
        viewModel.takePhoto(hasFlash = true, location = GeoPoint(1.0, 2.0)) { useFlash ->
            usedFlash = useFlash
            "/tmp/photo.jpg"
        }
        assertTrue(viewModel.uiState.value.isCapturingPhoto)
        advanceUntilIdle()
        assertEquals(true, usedFlash)
        val state = viewModel.uiState.value
        assertFalse(state.isCapturingPhoto)
        val draft = assertNotNull(state.snap).draft
        assertEquals(MemoryType.PHOTO, draft.type)
        assertEquals(listOf<Byte>(1, 2, 3, 0), (draft.media as MediaSource.Bytes).bytes.toList())
        assertEquals("jpg", (draft.media as MediaSource.Bytes).extension)
        assertEquals(GeoPoint(1.0, 2.0), draft.location)
        assertEquals(now, draft.capturedAt)
        // The camera's file was filtered, then deleted.
        assertEquals(listOf(Filter.NORMAL), renderedFilters)
        assertEquals(listOf("/tmp/photo.jpg"), deletedCaptures)
    }

    @Test
    fun theSelectedFilterIsAppliedToThePhoto() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.swipeFilter(forward = true)
        viewModel.takePhoto(hasFlash = false, location = null) { "/tmp/photo.jpg" }
        advanceUntilIdle()
        val draft = assertNotNull(viewModel.uiState.value.snap).draft
        assertEquals(Filter.NORMAL.next(), draft.filter)
        assertEquals(listOf(Filter.NORMAL.next()), renderedFilters)
    }

    @Test
    fun theCaptureIsDeletedEvenIfTheFilterFails() = runTest(dispatcher) {
        val viewModel = CameraViewModel(
            saveMemory = saveMemory,
            readCapture = readCapture,
            discardCapture = discardCapture,
            photoFilterRenderer = { _, _ -> error("Out of memory") },
            clock = clock,
            timeSource = testScheduler.timeSource,
        ).apply { onCameraReady(minZoom = 1.0, maxZoom = 20.0, zoom = 1.0) }
        viewModel.takePhoto(hasFlash = false, location = null) { "/tmp/photo.jpg" }
        advanceTimeBy(1.seconds)
        assertNull(viewModel.uiState.value.snap)
        assertEquals("Couldn't take the photo: Out of memory", viewModel.uiState.value.message)
        assertEquals(listOf("/tmp/photo.jpg"), deletedCaptures)
    }

    @Test
    fun theFrontCameraLightsTheScreenUpBeforeCapturing() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.flipCamera()
        viewModel.onCameraReady(minZoom = 1.0, maxZoom = 4.0, zoom = 1.0)
        viewModel.toggleFlash()
        var screenFlashWhileCapturing = false
        viewModel.takePhoto(hasFlash = false, location = null) { useFlash ->
            assertFalse(useFlash)
            screenFlashWhileCapturing = viewModel.uiState.value.isScreenFlashVisible
            "/tmp/selfie.jpg"
        }
        runCurrent()
        assertTrue(viewModel.uiState.value.isScreenFlashVisible)
        advanceUntilIdle()
        assertTrue(screenFlashWhileCapturing)
        assertFalse(viewModel.uiState.value.isScreenFlashVisible)
    }

    @Test
    fun aFailedPhotoShowsAMessage() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.takePhoto(hasFlash = false, location = null) { error("No camera") }
        advanceTimeBy(100.milliseconds)
        val state = viewModel.uiState.value
        assertNull(state.snap)
        assertFalse(state.isCapturingPhoto)
        assertEquals("Couldn't take the photo: No camera", state.message)
    }

    @Test
    fun noCaptureBeforeTheCameraIsReady() = runTest(dispatcher) {
        val viewModel = viewModel()
        var captured = false
        viewModel.takePhoto(hasFlash = false, location = null) { "/tmp/p.jpg".also { captured = true } }
        advanceUntilIdle()
        assertFalse(captured)
    }

    @Test
    fun aRecordingStopsAndOpensTheSnapPreview() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        val recorder = FakeRecorder()
        viewModel.startRecording(location = null, starter = recorder, thumbnail = { byteArrayOf(1) })
        runCurrent()
        advanceTimeBy(30.seconds + 1.milliseconds)
        assertEquals(0.5f, viewModel.uiState.value.recording?.progress)
        viewModel.stopRecording()
        assertEquals(true, viewModel.uiState.value.recording?.isStopping)
        runCurrent()
        assertTrue(recorder.stopped)
        recorder.finish("/tmp/video.mp4")
        runCurrent()
        val state = viewModel.uiState.value
        assertNull(state.recording)
        val draft = assertNotNull(state.snap).draft
        assertEquals(MemoryType.VIDEO, draft.type)
        assertEquals(MediaSource.File("/tmp/video.mp4"), draft.media)
        val duration = assertNotNull(draft.duration)
        assertTrue(duration >= 30.seconds && duration < 31.seconds, "Unexpected duration $duration")
        assertEquals(Filter.NORMAL, draft.filter)
        assertEquals(listOf<Byte>(1), draft.thumbnail?.toList())
    }

    @Test
    fun thePreviewHasNoFilterWhileRecordingAndItsThumbnailIsTakenOnceStarted() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.swipeFilter(forward = true)
        assertEquals(Filter.BLACK_AND_WHITE, viewModel.uiState.value.previewFilter)
        val recorder = FakeRecorder()
        var thumbnailTakenAfterStart: Boolean? = null
        viewModel.startRecording(location = null, starter = recorder, thumbnail = {
            thumbnailTakenAfterStart = recorder.started
            null
        })
        assertEquals(Filter.NORMAL, viewModel.uiState.value.previewFilter)
        runCurrent()
        assertEquals(true, thumbnailTakenAfterStart)
        // Swiping while recording doesn't change the filter.
        viewModel.swipeFilter(forward = true)
        assertEquals(Filter.BLACK_AND_WHITE, viewModel.uiState.value.filter)
        viewModel.stopRecording()
        runCurrent()
        recorder.finish("/tmp/video.mp4")
        runCurrent()
        assertEquals(Filter.NORMAL, assertNotNull(viewModel.uiState.value.snap).draft.filter)
    }

    @Test
    fun theProgressStopsUpdatingAtTheMaximumDuration() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.startRecording(location = null, starter = FakeRecorder(), thumbnail = { null })
        runCurrent()
        // The recorder stops by itself at its max duration: the progress loop must not run forever meanwhile.
        advanceTimeBy(CaptureRules.maxVideoDuration + 1.seconds)
        assertEquals(1f, viewModel.uiState.value.recording?.progress)
        advanceUntilIdle()
        assertEquals(1f, viewModel.uiState.value.recording?.progress)
    }

    @Test
    fun releasingTheShutterWhileTheRecordingStartsStopsIt() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        val recorder = FakeRecorder()
        viewModel.startRecording(location = null, starter = recorder, thumbnail = { null })
        // The finger is lifted before the recording started.
        viewModel.stopRecording()
        assertFalse(recorder.stopped)
        runCurrent()
        assertTrue(recorder.started)
        runCurrent()
        assertTrue(recorder.stopped)
    }

    @Test
    fun aFailedRecordingShowsAMessage() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        val recorder = FakeRecorder()
        viewModel.startRecording(location = null, starter = recorder, thumbnail = { null })
        runCurrent()
        recorder.fail(IllegalStateException("Disk full"))
        runCurrent()
        val state = viewModel.uiState.value
        assertNull(state.recording)
        assertNull(state.snap)
        assertEquals("Couldn't record the video: Disk full", state.message)
    }

    @Test
    fun slidingWhileRecordingZoomsFromTheStartZoom() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.onPinch(2f)
        viewModel.startRecording(location = null, starter = FakeRecorder(), thumbnail = { null })
        runCurrent()
        viewModel.onRecordingSlide(1f)
        assertEquals(8.0, viewModel.uiState.value.zoom)
        viewModel.onRecordingSlide(0f)
        assertEquals(2.0, viewModel.uiState.value.zoom)
    }

    @Test
    fun pinchingZoomsWithinTheRange() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.onPinch(3f)
        assertEquals(3.0, viewModel.uiState.value.zoom)
        viewModel.onPinch(10f)
        assertEquals(8.0, viewModel.uiState.value.zoom)
        viewModel.onPinch(0.01f)
        assertEquals(1.0, viewModel.uiState.value.zoom)
    }

    @Test
    fun flippingResetsTheZoomButNotWhileRecording() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.flipCamera()
        assertEquals(CameraFacing.FRONT, viewModel.uiState.value.facing)
        assertNull(viewModel.uiState.value.zoom)
        viewModel.onCameraReady(minZoom = 1.0, maxZoom = 4.0, zoom = 1.0)
        viewModel.startRecording(location = null, starter = FakeRecorder(), thumbnail = { null })
        viewModel.flipCamera()
        assertEquals(CameraFacing.FRONT, viewModel.uiState.value.facing)
    }

    @Test
    fun swipingChangesTheFilterAndShowsItsNameBriefly() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.swipeFilter(forward = true)
        assertEquals(Filter.NORMAL.next(), viewModel.uiState.value.filter)
        assertTrue(viewModel.uiState.value.isFilterNameVisible)
        viewModel.swipeFilter(forward = false)
        viewModel.swipeFilter(forward = false)
        assertEquals(Filter.NORMAL.previous(), viewModel.uiState.value.filter)
        advanceTimeBy(CameraViewModel.FILTER_NAME_DURATION + 1.milliseconds)
        assertFalse(viewModel.uiState.value.isFilterNameVisible)
    }

    @Test
    fun savingTheSnapSavesItToTheMemories() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.takePhoto(hasFlash = false, location = null) { "/tmp/photo.jpg" }
        advanceUntilIdle()
        viewModel.saveSnap()
        assertEquals(true, viewModel.uiState.value.snap?.isSaving)
        runCurrent()
        assertEquals(1, saved.size)
        assertNull(viewModel.uiState.value.snap)
        assertEquals("Saved to Memories", viewModel.uiState.value.message)
    }

    @Test
    fun discardingTheSnapDiscardsItsCapture() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.takePhoto(hasFlash = false, location = null) { "/tmp/photo.jpg" }
        advanceUntilIdle()
        viewModel.discardSnap()
        assertNull(viewModel.uiState.value.snap)
        runCurrent()
        assertEquals(1, discarded.size)
        assertTrue(saved.isEmpty())
    }

    /** A [RecordingStarter] whose recording the test finishes or fails. */
    private class FakeRecorder : RecordingStarter {
        var started = false
        var stopped = false
        private var onFinished: ((String) -> Unit)? = null
        private var onFailed: ((Throwable) -> Unit)? = null

        override suspend fun start(onFinished: (String) -> Unit, onFailed: (Throwable) -> Unit): suspend () -> Unit {
            started = true
            this.onFinished = onFinished
            this.onFailed = onFailed
            return { stopped = true }
        }

        fun finish(path: String) = onFinished?.invoke(path)

        fun fail(error: Throwable) = onFailed?.invoke(error)
    }
}
