package org.videolan.vlc.util

/**
 * Helper object providing pure, testable calculations for sleep timer expiration and delay.
 */
object SleepTimerCalculator {

    const val MAX_DELAY_MS = 30_000L
    const val MIN_DELAY_MS = 500L
    const val MEDIA_END_POLL_DELAY_MS = 1000L

    /**
     * Determines whether playback should be stopped based on time and media completion.
     */
    fun shouldStop(
        targetTimeMs: Long,
        nowMs: Long,
        waitForMediaEnd: Boolean,
        mediaEndReached: Boolean
    ): Boolean {
        val timerExpired = nowMs >= targetTimeMs
        return if (waitForMediaEnd) timerExpired && mediaEndReached else timerExpired
    }

    /**
     * Computes the adaptive delay (in milliseconds) before the next timer evaluation.
     */
    fun computeDelay(
        targetTimeMs: Long,
        nowMs: Long,
        waitForMediaEnd: Boolean
    ): Long {
        val remaining = targetTimeMs - nowMs
        return if (waitForMediaEnd && remaining <= 0) {
            MEDIA_END_POLL_DELAY_MS
        } else {
            minOf(maxOf(remaining, MIN_DELAY_MS), MAX_DELAY_MS)
        }
    }
}
