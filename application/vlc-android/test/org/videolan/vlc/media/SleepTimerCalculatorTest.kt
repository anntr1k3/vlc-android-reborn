package org.videolan.vlc.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.videolan.vlc.util.SleepTimerCalculator

class SleepTimerCalculatorTest {

    @Test
    fun shouldStop_whenTimerNotExpired_returnsFalse() {
        val target = 100_000L
        val now = 50_000L

        assertFalse(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = false, mediaEndReached = false))
        assertFalse(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = true, mediaEndReached = false))
        assertFalse(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = true, mediaEndReached = true))
    }

    @Test
    fun shouldStop_whenTimerExpiredAndNotWaitingForMediaEnd_returnsTrue() {
        val target = 100_000L
        val now = 100_001L

        assertTrue(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = false, mediaEndReached = false))
    }

    @Test
    fun shouldStop_whenTimerExpiredAndWaitingForMediaEnd_returnsTrueOnlyWhenEndReached() {
        val target = 100_000L
        val now = 105_000L

        assertFalse(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = true, mediaEndReached = false))
        assertTrue(SleepTimerCalculator.shouldStop(target, now, waitForMediaEnd = true, mediaEndReached = true))
    }

    @Test
    fun computeDelay_whenRemainingIsLarge_capsAtMaxDelay() {
        val target = 100_000L
        val now = 10_000L // 90 seconds remaining

        val delay = SleepTimerCalculator.computeDelay(target, now, waitForMediaEnd = false)
        assertEquals(SleepTimerCalculator.MAX_DELAY_MS, delay)
    }

    @Test
    fun computeDelay_whenRemainingIsModerate_returnsExactRemaining() {
        val target = 100_000L
        val now = 85_000L // 15 seconds remaining

        val delay = SleepTimerCalculator.computeDelay(target, now, waitForMediaEnd = false)
        assertEquals(15_000L, delay)
    }

    @Test
    fun computeDelay_whenRemainingIsVerySmall_floorsAtMinDelay() {
        val target = 100_000L
        val now = 99_800L // 200ms remaining

        val delay = SleepTimerCalculator.computeDelay(target, now, waitForMediaEnd = false)
        assertEquals(SleepTimerCalculator.MIN_DELAY_MS, delay)
    }

    @Test
    fun computeDelay_whenExpiredAndWaitingForMediaEnd_returnsPollDelay() {
        val target = 100_000L
        val now = 105_000L

        val delay = SleepTimerCalculator.computeDelay(target, now, waitForMediaEnd = true)
        assertEquals(SleepTimerCalculator.MEDIA_END_POLL_DELAY_MS, delay)
    }
}
