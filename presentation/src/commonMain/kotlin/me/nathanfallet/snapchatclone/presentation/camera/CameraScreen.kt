package me.nathanfallet.snapchatclone.presentation.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nathanfallet.snapchatclone.domain.models.CaptureRules
import me.nathanfallet.snapchatclone.presentation.components.CircleIconButton
import me.nathanfallet.snapchatclone.presentation.components.MessageBanner
import me.nathanfallet.snapchatclone.presentation.filters.FilterPainter
import me.nathanfallet.snapchatclone.presentation.snap.SnapPreviewScreen
import me.nathanfallet.snapchatclone.presentation.theme.Accent
import me.nathanfallet.snapchatclone.presentation.theme.AppIcons
import me.nathanfallet.snapchatclone.presentation.theme.Recording
import me.nathanfallet.snapchatclone.presentation.theme.Scrim
import me.nathanfallet.visioncamera.CameraControllerConfiguration
import me.nathanfallet.visioncamera.CameraDevice
import me.nathanfallet.visioncamera.CapturePhotoSettings
import me.nathanfallet.visioncamera.FlashMode
import me.nathanfallet.visioncamera.Location
import me.nathanfallet.visioncamera.PhotoOutputOptions
import me.nathanfallet.visioncamera.RecorderSettings
import me.nathanfallet.visioncamera.TargetCameraPosition
import me.nathanfallet.visioncamera.TargetPhotoContainerFormat
import me.nathanfallet.visioncamera.TargetVideoPixelFormat
import me.nathanfallet.visioncamera.VideoOutputOptions
import me.nathanfallet.visioncamera.barcode.TargetBarcodeFormat
import me.nathanfallet.visioncamera.barcode.compose.rememberBarcodeScanner
import me.nathanfallet.visioncamera.compose.rememberCameraDevice
import me.nathanfallet.visioncamera.compose.rememberCameraPermission
import me.nathanfallet.visioncamera.compose.rememberMicrophonePermission
import me.nathanfallet.visioncamera.compose.rememberPhotoOutput
import me.nathanfallet.visioncamera.compose.rememberVideoOutput
import me.nathanfallet.visioncamera.location.LocationAccuracy
import me.nathanfallet.visioncamera.location.compose.rememberLocation
import me.nathanfallet.visioncamera.skia.compose.SkiaCamera
import me.nathanfallet.visioncamera.skia.compose.rememberSkiaCameraRef
import kotlin.math.roundToInt

/**
 * The camera screen: a full-screen [SkiaCamera] with Snapchat's gestures and controls, and the preview of the
 * captured snap.
 *
 * @param viewModel The ViewModel of the screen.
 * @param isVisible Whether the screen is visible: the camera stops when another screen covers it.
 * @param onOpenMemories Opens the Memories (swipe up, or the Memories button).
 */
@Composable
fun CameraScreen(viewModel: CameraViewModel, isVisible: Boolean, onOpenMemories: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberCameraPermission()
    val microphonePermission = rememberMicrophonePermission()
    // Like VisionCamera's example app: the location is embedded in the photos and saved with the memories.
    val location = rememberLocation(accuracy = LocationAccuracy.BALANCED, distanceFilter = LOCATION_DISTANCE_FILTER)

    LaunchedEffect(Unit) {
        // Asked one after the other, so the system prompts don't overlap.
        if (!cameraPermission.hasPermission && cameraPermission.canRequestPermission) {
            cameraPermission.requestPermission()
        }
        if (cameraPermission.hasPermission) {
            if (microphonePermission.canRequestPermission) microphonePermission.requestPermission()
            if (!location.hasPermission) location.requestPermission()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.hasPermission) {
            CameraContent(
                state = state,
                viewModel = viewModel,
                isActive = isVisible && state.snap == null,
                enableAudio = microphonePermission.hasPermission,
                location = location.currentLocation,
                onOpenMemories = onOpenMemories,
            )
        } else {
            PermissionContent(
                canRequest = cameraPermission.canRequestPermission,
                onRequest = { cameraPermission.requestPermission() },
            )
        }
        state.snap?.let { snap ->
            SnapPreviewScreen(snap = snap, onSave = viewModel::saveSnap, onDiscard = viewModel::discardSnap)
        }
        MessageBanner(
            message = state.message,
            modifier = Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 64.dp),
        )
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun CameraContent(
    state: CameraUiState,
    viewModel: CameraViewModel,
    isActive: Boolean,
    enableAudio: Boolean,
    location: Location?,
    onOpenMemories: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val position = if (state.facing == CameraFacing.FRONT) TargetCameraPosition.FRONT else TargetCameraPosition.BACK
    val device = rememberCameraDevice(position)
    val photoOutput = rememberPhotoOutput(PhotoOutputOptions(containerFormat = TargetPhotoContainerFormat.JPEG))
    val videoOutput = rememberVideoOutput(VideoOutputOptions(enableAudio = enableAudio))
    val camera = rememberSkiaCameraRef()
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    // The gestures are installed once: they read the latest state and callbacks through these.
    val currentState by rememberUpdatedState(state)
    val currentOnOpenMemories by rememberUpdatedState(onOpenMemories)
    // Read on the Frame output's thread, which draws the preview: the painter compiles each filter's shader once.
    val previewFilter by rememberUpdatedState(state.previewFilter)
    val filterPainter = remember { FilterPainter() }
    // QR codes are scanned in the frames drawn by the SkiaCamera, while a scan is in progress (a long press).
    val barcodeScanner = rememberBarcodeScanner(listOf(TargetBarcodeFormat.QR_CODE))

    // Applies the zoom of the ViewModel (pinch, slide while recording) to the camera.
    val controller = camera.controller
    LaunchedEffect(controller, state.zoom) {
        val zoom = state.zoom ?: return@LaunchedEffect
        attempt { controller?.setZoom(zoom) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SkiaCamera(
            device = device ?: position,
            isActive = isActive,
            onFrame = { frame, render ->
                render { (frameTexture, canvas) -> filterPainter.draw(canvas, frameTexture, previewFilter) }
                if (viewModel.isScanning) {
                    scanCodes { barcodeScanner.scanCodes(frame).mapNotNull { it.rawValue } }
                        ?.let(viewModel::onCodesScanned)
                }
                frame.dispose()
            },
            outputs = listOf(photoOutput, videoOutput),
            // The barcode scanner (ML Kit on Android) reads YUV frames; the SkiaCamera still draws them as textures.
            pixelFormat = TargetVideoPixelFormat.YUV,
            controllerConfiguration = CameraControllerConfiguration(
                enableLowLightBoost = state.isNightModeEnabled && device?.supportsLowLightBoost == true,
            ),
            ref = camera,
            onControllerChanged = { it?.let { viewModel.onCameraReady(it.minZoom, it.maxZoom, it.zoom) } },
            onError = viewModel::onCameraError,
            modifier = Modifier.fillMaxSize().cameraGestures(
                onTap = { offset ->
                    focusPoint = offset
                    scope.launch { attempt { camera.focusTo(offset) } }
                },
                onDoubleTap = viewModel::flipCamera,
                onLongPress = { viewModel.startScan() },
                onPinch = viewModel::onPinch,
                onSwipe = { swipe ->
                    when (swipe) {
                        Swipe.LEFT -> viewModel.swipeFilter(forward = true)
                        Swipe.RIGHT -> viewModel.swipeFilter(forward = false)
                        Swipe.UP -> if (!currentState.isBusy) currentOnOpenMemories()
                        Swipe.DOWN -> Unit
                    }
                },
            ),
        )

        focusPoint?.let { FocusIndicator(it, onFinished = { focusPoint = null }) }

        FilterName(
            name = state.filter.displayName,
            isVisible = state.isFilterNameVisible,
            modifier = Modifier.align(Alignment.Center),
        )

        CameraControls(
            state = state,
            device = device,
            onFlip = viewModel::flipCamera,
            onToggleFlash = viewModel::toggleFlash,
            onToggleNightMode = viewModel::toggleNightMode,
            onOpenMemories = onOpenMemories,
            onTakePhoto = {
                val hasFlash = device?.hasFlash == true
                viewModel.takePhoto(hasFlash = hasFlash, location = location?.toGeoPoint()) { useFlash ->
                    val settings = CapturePhotoSettings(
                        flashMode = if (useFlash) FlashMode.ON else FlashMode.OFF,
                        location = location,
                    )
                    // Saved to a file, which carries the orientation (and mirroring) in its EXIF metadata: the
                    // ViewModel then draws it upright with the selected filter, at full resolution.
                    photoOutput.capturePhoto(settings).use { it.saveToTemporaryFile() }
                }
            },
            onStartRecording = {
                viewModel.startRecording(
                    location = location?.toGeoPoint(),
                    starter = { onFinished, onFailed ->
                        val recorder = videoOutput.createRecorder(
                            RecorderSettings(location = location, maxDuration = CaptureRules.maxVideoDuration),
                        )
                        recorder.startRecording(
                            onRecordingFinished = { path, _ -> onFinished(path) },
                            onRecordingError = onFailed,
                        )
                        val stop: suspend () -> Unit = { recorder.stopRecording() }
                        stop
                    },
                    thumbnail = { camera.snapshotThumbnail() },
                )
            },
            onRecordingSlide = viewModel::onRecordingSlide,
            onStopRecording = viewModel::stopRecording,
        )

        ScanOverlay(scan = state.scan, onDismiss = viewModel::dismissScan)

        // The screen flash: the front camera has no flash, the screen lights the face up instead.
        if (state.isScreenFlashVisible) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White))
        }
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun CameraControls(
    state: CameraUiState,
    device: CameraDevice?,
    onFlip: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleNightMode: () -> Unit,
    onOpenMemories: () -> Unit,
    onTakePhoto: () -> Unit,
    onStartRecording: () -> Unit,
    onRecordingSlide: (Float) -> Unit,
    onStopRecording: () -> Unit,
) {
    val isRecording = state.recording != null
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        // The toolbar, on the right like in Snapchat.
        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircleIconButton(AppIcons.Flip, contentDescription = "Flip camera", onClick = onFlip)
                CircleIconButton(
                    icon = if (state.isFlashEnabled) AppIcons.FlashOn else AppIcons.FlashOff,
                    contentDescription = if (state.isFlashEnabled) "Turn flash off" else "Turn flash on",
                    onClick = onToggleFlash,
                    tint = if (state.isFlashEnabled) Accent else Color.White,
                )
                // Low-light boost is only offered where the device supports it.
                if (device?.supportsLowLightBoost == true) {
                    CircleIconButton(
                        icon = AppIcons.Night,
                        contentDescription = "Night mode",
                        onClick = onToggleNightMode,
                        tint = if (state.isNightModeEnabled) Accent else Color.White,
                    )
                }
            }
        }

        state.recording?.let { RecordingBadge(it.progress, modifier = Modifier.align(Alignment.TopCenter)) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            val zoom = state.displayedZoom
            if (zoom != null && zoom.isZoomedIn()) {
                Text(
                    "${zoom.formatZoom()}x",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Scrim, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            ShutterButton(
                recording = state.recording,
                enabled = !state.isBusy || isRecording,
                slideDistance = SHUTTER_SLIDE_DISTANCE,
                onTap = onTakePhoto,
                onHoldStart = onStartRecording,
                onHoldSlide = onRecordingSlide,
                onHoldEnd = onStopRecording,
            )
        }

        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 30.dp),
        ) {
            CircleIconButton(AppIcons.Memories, contentDescription = "Memories", onClick = onOpenMemories)
        }
    }
}

/** The recording timer, at the top of the screen. */
@Composable
private fun RecordingBadge(progress: Float, modifier: Modifier = Modifier) {
    val seconds = (progress * CaptureRules.maxVideoDuration.inWholeSeconds).toInt()
    Text(
        "● ${seconds / SECONDS_PER_MINUTE}:${(seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(top = 8.dp)
            .background(Recording, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

/** The name of the filter, shown briefly in the middle of the screen after a swipe. */
@Composable
private fun FilterName(name: String, isVisible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Text(
            name,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 12f),
            ),
        )
    }
}

/** A ring shrinking where the user tapped to focus. */
@Composable
private fun FocusIndicator(point: Offset, onFinished: () -> Unit) {
    val scale = remember(point) { Animatable(1.4f) }
    LaunchedEffect(point) {
        scale.animateTo(1f)
        delay(FOCUS_INDICATOR_MILLIS)
        onFinished()
    }
    val radius = with(LocalDensity.current) { FOCUS_RADIUS.toPx() }
    Canvas(
        modifier = Modifier
            .offset { IntOffset((point.x - radius).roundToInt(), (point.y - radius).roundToInt()) }
            .size(FOCUS_RADIUS * 2),
    ) {
        drawCircle(Color.White, radius = radius * scale.value * 0.9f, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun PermissionContent(canRequest: Boolean, onRequest: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
    ) {
        Text("Snap Clone KMP", color = Accent, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(
            "The camera is the heart of this app. Allow it to take photos and videos." +
                if (canRequest) "" else "\nOpen the Settings of your phone to allow it.",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (canRequest) {
            Button(onClick = { scope.launch { onRequest() } }) {
                Text("Allow the camera", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private const val LOCATION_DISTANCE_FILTER = 10.0
private const val SECONDS_PER_MINUTE = 60
private const val FOCUS_INDICATOR_MILLIS = 600L
private val FOCUS_RADIUS = 36.dp
/** How far the finger holding the shutter slides up to reach the maximum zoom. */
private val SHUTTER_SLIDE_DISTANCE = 320.dp
