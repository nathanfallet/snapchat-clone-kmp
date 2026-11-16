package me.nathanfallet.snapchatclone.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CaptureRulesTest {
    @Test
    fun videosLastAtMostOneMinute() {
        assertEquals(60.seconds, CaptureRules.maxVideoDuration)
    }

    @Test
    fun progressGrowsWithTheRecordingAndIsClamped() {
        assertEquals(0f, CaptureRules.recordingProgress(Duration.ZERO))
        assertEquals(0.5f, CaptureRules.recordingProgress(30.seconds))
        assertEquals(1f, CaptureRules.recordingProgress(60.seconds))
        assertEquals(1f, CaptureRules.recordingProgress(75.seconds))
        assertEquals(0f, CaptureRules.recordingProgress((-1).seconds))
    }
}
