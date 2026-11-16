package me.nathanfallet.snapchatclone.presentation.camera

/**
 * Starts a video recording with the camera, for [CameraViewModel.startRecording]. Implemented by the camera screen
 * with KMP Vision Camera's `Recorder`, and by fakes in tests.
 */
fun interface RecordingStarter {
    /**
     * Starts recording, and returns once the recording has started.
     *
     * @param onFinished Called with the path of the video file once the recording finished (stopped or reached its
     * maximum duration), from any thread.
     * @param onFailed Called if the recording failed, from any thread.
     * @return A function stopping the recording. [onFinished] is called once the file is complete.
     */
    suspend fun start(onFinished: (path: String) -> Unit, onFailed: (Throwable) -> Unit): suspend () -> Unit
}
