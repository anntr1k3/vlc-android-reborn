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
     *
     * While plenty of time remains, the delay is capped at [MAX_DELAY_MS] so the
     * service does not wake every second. Once remaining time is below that cap,
     * the delay is the remaining time itself — never rounded up — so the last
     * tick cannot overshoot the target. [MIN_DELAY_MS] is only a defensive floor
     * after expiry when [shouldStop] did not already end the job.
     */
    fun computeDelay(
        targetTimeMs: Long,
        nowMs: Long,
        waitForMediaEnd: Boolean
    ): Long {
        val remaining = targetTimeMs - nowMs
        return when {
            remaining <= 0L && waitForMediaEnd -> MEDIA_END_POLL_DELAY_MS
            remaining <= 0L -> MIN_DELAY_MS
            else -> minOf(remaining, MAX_DELAY_MS)
        }
    }
}
